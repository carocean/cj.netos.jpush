package cj.netos.jpush.device.net;

import cj.netos.jpush.IJPushServiceProvider;
import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.PackFrame;
import cj.netos.jpush.device.*;
import cj.netos.jpush.util.PropUtil;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.util.TcpFrameBox;
import cj.ultimate.util.StringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TcpConnection implements IConnection, IJPushServiceProvider {
    private final IOnopen onopen;
    private final IOnclose onclose;
    EventLoopGroup exepool;
    private Channel channel;
    private String protocol;
    private String host;
    private int port;
    private long heartbeat;
    private Map<String, String> props;
    private int workThreadCount;

    DefaultPipelineCombination pipelineCombination;

    public TcpConnection(IOnopen onopen, IOnclose onclose, IOnerror onerror, IOnmessage onmessage) {
        pipelineCombination = new DefaultPipelineCombination(onmessage, onerror);
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

        b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .handler(new TcpClientGatewaySocketInitializer());
        try {
            this.channel = b.connect(ip, port).sync().channel();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void parseProps(Map<String, String> props) {
        String strheartbeat = PropUtil.getValue(props
                .get("heartbeat"));
        if (StringUtil.isEmpty(strheartbeat)) {
            strheartbeat = "0";
        }
        this.heartbeat = Long.valueOf(strheartbeat);


        String workThreadCount = PropUtil.getValue(props
                .get("workThreadCount"));
        if (StringUtil.isEmpty(workThreadCount)) {
            workThreadCount = "0";
        }
        this.workThreadCount = Integer.valueOf(workThreadCount);

        CJSystem.logging().info(getClass(), String.format("连接属性：workThreadCount=%s,heartbeat=%s",
                workThreadCount, heartbeat));

    }

    @Override
    public void send(JPushFrame frame) {
        PackFrame pack = new PackFrame((byte) 1, frame);
        byte[] box = TcpFrameBox.box(pack.toBytes());
        pack.dispose();
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(box, 0, box.length);
        channel.writeAndFlush(bb);
    }

    @Override
    public boolean isConnected() {
        return channel.isWritable();
    }

    @Override
    public void close() {
        channel.close();
    }

    class TcpClientGatewaySocketInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(81920, 0, 4, 0, 4));
            if (heartbeat > 0) {
                pipeline.addLast(new IdleStateHandler(0, heartbeat, 0, TimeUnit.SECONDS));
            }
            pipeline.addLast(new TcpClientHandler(TcpConnection.this));
        }

    }

}
