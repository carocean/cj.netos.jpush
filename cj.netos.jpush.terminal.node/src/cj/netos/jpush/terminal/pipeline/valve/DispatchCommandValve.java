package cj.netos.jpush.terminal.pipeline.valve;

import cj.netos.jpush.*;
import cj.netos.jpush.ChannelWriter;
import cj.netos.jpush.terminal.ITerminalCommand;
import cj.netos.jpush.terminal.cmd.*;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class DispatchCommandValve extends ChannelWriter implements IValve {
    IJPushServiceProvider site;
    Map<String, ITerminalCommand> commandMap;

    public DispatchCommandValve(IJPushServiceProvider site) {
        this.site = site;
        commandMap = new HashMap<>();
        ITerminalCommand login = new LoginTerminalCommand();
        commandMap.put(login.cmd(), login);
        ITerminalCommand ls = new LsTerminalCommand();
        commandMap.put(ls.cmd(), ls);
        ITerminalCommand pause = new PauseTerminalCommand();
        commandMap.put(pause.cmd(), pause);
        ITerminalCommand resume = new ResumeTerminalCommand();
        commandMap.put(resume.cmd(), resume);
        ITerminalCommand adminls = new AdminLsTerminalCommand();
        commandMap.put(adminls.cmd(), adminls);
        ITerminalCommand adminview = new AdminViewTerminalCommand();
        commandMap.put(adminview.cmd(), adminview);
    }

    @Override
    public void flow(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        if (!commandMap.containsKey(frame.command())) {
            nextError(new CircuitException("404", "不支持的指令:" + frame.command()), pipeline);
            return;
        }
        ITerminalCommand command = commandMap.get(frame.command());
        command.exec(frame, pipeline);
    }

    @Override
    public void nextError(Throwable error, IPipeline pipeline) throws CircuitException {
        //发送给客户端
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        StringWriter buffer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(buffer);
        error.printStackTrace(printWriter);
        map.put("cause", buffer.toString());
        bb.writeBytes(new Gson().toJson(map).getBytes());

        JPushFrame back = new JPushFrame(String.format("error / NET/1.0"), bb);
        EndPort endPort = pipeline.endPort();
        if (endPort != null) {
            back.head("sender-person", endPort.getPerson());
            back.head("sender-device", endPort.getDevice());
        }
        CircuitException ce = CircuitException.search(error);
        if (ce != null) {
            back.head("status", ce.getStatus());
            back.head("message", ce.getMessage() + "");
        } else {
            back.head("status", "500");
            back.head("message", error.getMessage() + "");
        }
        Channel channel = (Channel) pipeline.attachment();
        writeChannel(channel, back);
        //出错了而且没有登录的终结点则关闭通道，其它错误不关闭
        if (pipeline.endPort() == null) {
            channel.close();
        }
    }

}
