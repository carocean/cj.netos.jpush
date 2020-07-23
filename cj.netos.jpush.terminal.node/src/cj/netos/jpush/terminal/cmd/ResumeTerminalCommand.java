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
import java.util.List;

public class ResumeTerminalCommand implements ITerminalCommand {

    @Override
    public String cmd() {
        return "resume";
    }

    @Override
    public void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        EndPort endPort = pipeline.endPort();
        IEndPortContainer endPortContainer = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        PersonEndPorts personEndPorts = endPortContainer.getPersonEndPorts(endPort.getPerson());
        boolean isConsumed=false;
        if (personEndPorts != null) {
            IRabbitMQConsumer rabbitMQConsumer = (IRabbitMQConsumer) pipeline.site().getService("$.terminal.rabbitMQConsumer");
            rabbitMQConsumer.consumePersonQueue(personEndPorts);
            isConsumed=personEndPorts.isConsumed();
        }
        JPushFrame response = new JPushFrame(String.format("resume / net/1.0"));
        response.head("sender-person", endPort.getPerson());
        response.head("sender-device", endPort.getDevice());
        response.head("nick-name", endPort.getNickName());
        response.head("is-consumed", isConsumed + "");
        endPort.writeFrame(response);
    }

}
