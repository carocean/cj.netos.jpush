package cj.netos.jpush.device.net;

import cj.netos.jpush.IJPushServiceProvider;
import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.device.*;
import cj.netos.jpush.util.PropUtil;
import cj.studio.ecm.CJSystem;
import cj.ultimate.util.StringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WSConnection implements IConnection, IJPushServiceProvider {
    private final IOnopen onopen;
    private final IOnclose onclose;
    EventLoopGroup exepool;
    private Channel channel;
    String device;
    private String protocol;
    private String host;
    private int port;
    private long heartbeat;
    private Map<String, String> props;
    private int workThreadCount;
    private String wspath;
    int maxContentLength;

    DefaultPipelineCombination pipelineCombination;


    public WSConnection(IOnopen onopen, IOnclose onclose, IOnerror onerror, IOnevent onevent, IOnmessage onmessage) {
        pipelineCombination = new DefaultPipelineCombination(onmessage, onerror, onevent);
        this.onopen = onopen;
        this.onclose = onclose;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Object getService(String serviceId) {
        if ("$.prop.heartbeat".equals(serviceId)) {
            return heartbeat;
        }
        if ("$.connection".equals(serviceId)) {
            return this;
        }
        if ("$.channel".equals(serviceId)) {
            return channel;
        }
        if ("$.pipelineCombination".equals(serviceId)) {
            return pipelineCombination;
        }
        return null;
    }

    @Override
    public void onclose() {
        if (onclose != null) {
            onclose.onclose();
        }
        if (exepool != null) {
            this.exepool.shutdownGracefully();
        }
        this.channel = null;
        if (props != null) {
            this.props.clear();
        }
        this.pipelineCombination = null;
    }

    @Override
    public void onopen() {
        if (onopen != null) {
            onopen.onopen();
        }
    }


    @Override
    public void connect(String protocol, String ip, int port, Map<String, String> props) {
        this.protocol = protocol;
        this.host = ip;
        this.port = port;
        this.props = props;
        parseProps(props);

        EventLoopGroup group = null;
        if (workThreadCount < 1) {
            group = new NioEventLoopGroup();
        } else {
            group = new NioEventLoopGroup(workThreadCount);
        }

        Bootstrap b = new Bootstrap();
        URI uri = null;
        String url = String.format("%s://%s:%s%s", this.protocol, this.host, this.port, this.wspath);
        try {
            uri = new URI(url);
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        CJSystem.logging().info(getClass(), "连接地址：" + url);
        HttpHeaders customHeaders = new DefaultHttpHeaders();
//		customHeaders.add("MyHeader", "MyValue");
        final WSClientHandler handler =
                new WSClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, false, customHeaders), WSConnection.this);

        try {
            b.group(group).channel(NioSocketChannel.class).handler(new WebsocketClientGatewaySocketInitializer("wss".equals(protocol), handler));
            this.channel = b.connect(ip, port).sync().channel();
            handler.handshakeFuture().sync();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void parseProps(Map<String, String> props) {
        String wspath = PropUtil.getValue(props.get("wspath"));
        if (StringUtil.isEmpty(wspath)) {
            wspath = "/websocket";
        }
        if (!wspath.startsWith("/")) {
            wspath = "/" + wspath;
        }
        this.wspath = wspath;

        String strheartbeat = PropUtil.getValue(props
                .get("heartbeat"));
        if (StringUtil.isEmpty(strheartbeat)) {
            strheartbeat = "0";
        }
        this.heartbeat = Long.valueOf(strheartbeat);
        //maxContentLength
        String strmaxContentLength = PropUtil.getValue(props
                .get("maxContentLength"));
        if (StringUtil.isEmpty(strmaxContentLength)) {
            strmaxContentLength = "2097152";
        }
        this.maxContentLength = Integer.valueOf(strmaxContentLength);


        String workThreadCount = PropUtil.getValue(props
                .get("workThreadCount"));
        if (StringUtil.isEmpty(workThreadCount)) {
            workThreadCount = "0";
        }
        this.workThreadCount = Integer.valueOf(workThreadCount);

        CJSystem.logging().info(getClass(), String.format("连接属性：workThreadCount=%s,heartbeat=%s,wspath=%s",
                workThreadCount, heartbeat, wspath));

    }

    @Override
    public void send(JPushFrame frame) {
        ByteBuf bb = Unpooled.buffer();
        byte[] b = frame.toBytes();
        frame.dispose();
        bb.writeBytes(b, 0, b.length);
        BinaryWebSocketFrame webSocketFrame = new BinaryWebSocketFrame(bb);
        channel.writeAndFlush(webSocketFrame);
    }

    @Override
    public boolean isConnected() {
        return channel.isWritable();
    }

    @Override
    public void close() {
        channel.close();
    }

    class WebsocketClientGatewaySocketInitializer extends ChannelInitializer<SocketChannel> {
        WSClientHandler handler;
        SslContext sslCtx;

        public WebsocketClientGatewaySocketInitializer(boolean SSL, WSClientHandler handler) throws SSLException {
            this.handler = handler;
            if (SSL) {
                sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
            }
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            if (sslCtx != null) {
                pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            }
            pipeline.addLast(new HttpClientCodec());
            pipeline.addLast(new WebSocketClientCompressionHandler());
            if (heartbeat > 0) {
                pipeline.addLast(new IdleStateHandler(0, heartbeat, 0, TimeUnit.SECONDS));
            }
            pipeline.addLast(new HttpObjectAggregator(maxContentLength));
            pipeline.addLast(handler);
        }

    }


}
