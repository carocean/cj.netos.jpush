package cj.netos.jpush.device.net;

import cj.netos.jpush.*;
import cj.netos.jpush.device.IConnection;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.CircuitException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

class WSClientHandler extends SimpleChannelInboundHandler<Object> {

    IJPushServiceProvider site;
    IPipeline pipeline;
    WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public WSClientHandler(WebSocketClientHandshaker handshaker, IJPushServiceProvider site) {
        this.handshaker = handshaker;
        this.site=site;
    }


    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        pipeline.error(cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
        IConnection connection =(IConnection) site.getService("$.connection");
        pipeline = new DefaultPipeline(site);
        IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
        combination.combine(pipeline);
        connection.onopen();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
        combination.demolish(pipeline);
        pipeline.dispose();
        pipeline = null;
        IConnection connection =(IConnection) site.getService("$.connection");
        connection.onclose();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 不管是读事件空闲还是写事件空闲都向服务器发送心跳包
            sendHeartbeatPacket(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHeartbeatPacket(ChannelHandlerContext ctx) throws CircuitException {
        WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
        ctx.channel().writeAndFlush(frame);
//        CJSystem.logging().debug(getClass(),"发送心跳包");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof PongWebSocketFrame) {
//            CJSystem.logging().debug(getClass(),"收到心跳包");
            return;
        }
        if (msg instanceof CloseWebSocketFrame) {
            ch.close();
            return;
        }
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
                    + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
        ByteBuf bb = null;
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame f = (TextWebSocketFrame) msg;
            bb = f.content();
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame f = (BinaryWebSocketFrame) msg;
            bb = f.content();
        } else {
            throw new EcmException("不支持此类消息：" + msg.getClass());
        }
        if (bb.readableBytes() == 0) {
            return;
        }
        byte[] b = new byte[bb.readableBytes()];
        bb.readBytes(b);

        JPushFrame frame = new JPushFrame(b);
        pipeline.input(frame);
    }
}