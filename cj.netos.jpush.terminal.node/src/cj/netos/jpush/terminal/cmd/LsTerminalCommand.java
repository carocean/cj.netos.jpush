package cj.netos.jpush.terminal.cmd;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IPipeline;
import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.IEndPortContainer;
import cj.netos.jpush.terminal.INodeConfig;
import cj.netos.jpush.terminal.ITerminalCommand;
import cj.netos.jpush.terminal.PersonEndPorts;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

public class LsTerminalCommand implements ITerminalCommand {

    @Override
    public String cmd() {
        return "ls";
    }

    @Override
    public void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        EndPort endPort = pipeline.endPort();
        IEndPortContainer endPortContainer = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        PersonEndPorts personEndPorts = endPortContainer.getPersonEndPorts(endPort.getPerson());
        ByteBuf bb = Unpooled.buffer();
        boolean isConsumed=false;
        if (personEndPorts != null) {
            isConsumed=personEndPorts.isConsumed();
            List<String> devices = new ArrayList<>();
            for (EndPort port : personEndPorts.endPorts()) {
                devices.add(port.getDevice());
            }
            bb.writeBytes(new Gson().toJson(devices).getBytes());
        }
        JPushFrame response = new JPushFrame(String.format("ls / net/1.0"), bb);
        response.head("sender-person", endPort.getPerson());
        response.head("sender-device", endPort.getDevice());
        response.head("nick-name", endPort.getNickName());
        response.head("is-consumed", isConsumed + "");
        endPort.writeFrame(response);
    }

}
