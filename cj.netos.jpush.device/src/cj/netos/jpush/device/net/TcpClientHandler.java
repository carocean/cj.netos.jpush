package cj.netos.jpush.device.net;

import cj.netos.jpush.*;
import cj.netos.jpush.device.IConnection;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.util.TcpFrameBox;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

class TcpClientHandler extends SimpleChannelInboundHandler<Object> {

    IJPushServiceProvider site;
    IPipeline pipeline;

    public TcpClientHandler(IJPushServiceProvider site) {
        this.site = site;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        pipeline.error(cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        pipeline = new DefaultPipeline(site);
        IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
        combination.combine(pipeline);
        IConnection connection =(IConnection) site.getService("$.connection");
        connection.onopen();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        IConnection connection =(IConnection) site.getService("$.connection");
        IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
        combination.demolish(pipeline);
        pipeline.dispose();
        pipeline = null;
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
        JPushFrame f = new JPushFrame("heartbeat / NET/1.0");
        PackFrame pack = new PackFrame((byte) 2, f);
        byte[] box = TcpFrameBox.box(pack.toBytes());
        pack.dispose();
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(box, 0, box.length);
        ctx.channel().writeAndFlush(bb);
//        CJSystem.logging().info(getClass(),"发送心跳包");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        if (bb.readableBytes() == 0) {
            return;
        }
        byte[] b = new byte[bb.readableBytes()];
        bb.readBytes(b);
//        bb.release();//系统会释放
        if (b.length < 1) {
            return;
        }
        PackFrame pack = new PackFrame(b);
        if (pack.isInvalid()) {
            return;
        }
        if (pack.isHeartbeat()) {
            return;
        }
        JPushFrame frame = pack.getFrame();
        if (frame == null) {
            return;
        }
        //上下文路径是网络名，二级路径是请求名
//        String networkName = frame.rootName();
//        frame.url(frame.relativeUrl());
        pipeline.input(frame);
    }
}