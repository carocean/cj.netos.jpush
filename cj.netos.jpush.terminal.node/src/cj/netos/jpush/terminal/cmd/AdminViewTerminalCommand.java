package cj.netos.jpush.terminal.cmd;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IPipeline;
import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.IEndPortContainer;
import cj.netos.jpush.terminal.IRabbitMQConsumer;
import cj.netos.jpush.terminal.ITerminalCommand;
import cj.netos.jpush.terminal.PersonEndPorts;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminViewTerminalCommand implements ITerminalCommand {

    @Override
    public String cmd() {
        return "adminView";
    }

    @Override
    public void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        EndPort endPort = pipeline.endPort();
        if (!endPort.roleIn("platform:administrators")) {
            JPushFrame response = new JPushFrame(String.format("adminView / net/1.0"));
            response.head("sender-person", endPort.getPerson());
            response.head("sender-device", endPort.getDevice());
            response.head("nick-name", endPort.getNickName());
            response.head("status", "801");
            response.head("message", "拒绝访问");
            endPort.writeFrame(response);
            return;
        }
        IEndPortContainer endPortContainer = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        PersonEndPorts personEndPorts = endPortContainer.getPersonEndPorts(frame.head("person"));
        ByteBuf bb = Unpooled.buffer();

        if (personEndPorts != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("person", personEndPorts.getPerson());
            map.put("nickName", personEndPorts.getNickName());
            map.put("isConsume", personEndPorts.isConsumed() + "");
            List<String> devices = new ArrayList<>();
            for (EndPort port : personEndPorts.endPorts()) {
                devices.add(port.getDevice());
            }
            map.put("devices", devices);
            bb.writeBytes(new Gson().toJson(map).getBytes());
        }
        JPushFrame response = new JPushFrame(String.format("adminView / net/1.0"), bb);
        response.head("sender-person", endPort.getPerson());
        response.head("sender-device", endPort.getDevice());
        response.head("nick-name", endPort.getNickName());
        PersonEndPorts owner = endPortContainer.getPersonEndPorts(endPort.getPerson());
        boolean isConsumed = owner == null ? false : owner.isConsumed();
        response.head("is-consumed", isConsumed + "");
        endPort.writeFrame(response);
    }

}
