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

import java.util.*;

public class AdminLsTerminalCommand implements ITerminalCommand {

    @Override
    public String cmd() {
        return "adminLs";
    }

    @Override
    public void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        EndPort endPort = pipeline.endPort();
        if (!endPort.roleIn("platform:administrators")) {
            JPushFrame response = new JPushFrame(String.format("adminLs / net/1.0"));
            response.head("sender-person", endPort.getPerson());
            response.head("sender-device", endPort.getDevice());
            response.head("nick-name", endPort.getNickName());
            response.head("status", "801");
            response.head("message", "拒绝访问");
            endPort.writeFrame(response);
            return;
        }
        IEndPortContainer endPortContainer = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        Set<String> persons = endPortContainer.listPerson();
        ByteBuf bb = Unpooled.buffer();
        List<Object> list = new ArrayList<>();
        for (String person : persons) {
            PersonEndPorts personEndPorts = endPortContainer.getPersonEndPorts(person);
            Map<String, Object> map = new HashMap<>();
            map.put("person", personEndPorts.getPerson());
            map.put("nickName", personEndPorts.getNickName());
            map.put("isConsume", personEndPorts.isConsumed() + "");
            List<String> devices = new ArrayList<>();
            for (EndPort port : personEndPorts.endPorts()) {
                devices.add(port.getDevice());
            }
            map.put("devices", devices);
            list.add(map);
        }
        bb.writeBytes(new Gson().toJson(list).getBytes());
        JPushFrame response = new JPushFrame(String.format("adminLs / net/1.0"), bb);
        response.head("sender-person", endPort.getPerson());
        response.head("sender-device", endPort.getDevice());
        response.head("online-count", endPortContainer.count() + "");
        endPort.writeFrame(response);
    }

}
