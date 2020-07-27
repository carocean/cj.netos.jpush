package cj.netos.jpush.terminal.cmd;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IPipeline;
import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.*;
import cj.netos.jpush.util.Encript;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.channel.Channel;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogoutTerminalCommand implements ITerminalCommand {

    @Override
    public String cmd() {
        return "logout";
    }

    //注册到rabbitmq
    //生成终结点
    //添加到终结点容器
    //将终结点设置到pipeline上
    @Override
    public void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        EndPort endPort = pipeline.endPort();
        IEndPortContainer endPortContainer = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        PersonEndPorts personEndPorts = endPortContainer.getPersonEndPorts(endPort.getPerson());
        boolean isConsumed = false;
        if (personEndPorts != null) {
            IRabbitMQConsumer rabbitMQConsumer = (IRabbitMQConsumer) pipeline.site().getService("$.terminal.rabbitMQConsumer");
            rabbitMQConsumer.stopConsumePersonQueue(personEndPorts);
            isConsumed = personEndPorts.isConsumed();
            endPortContainer.offline(endPort);
            pipeline.endPort(null);
        }
        JPushFrame response = new JPushFrame(String.format("offline / net/1.0"));
        response.head("to-person", endPort.getPerson());
        response.head("to-device", endPort.getDevice());
        response.head("nick-name", endPort.getNickName());
        response.head("is-consumed", isConsumed + "");
        endPort.writeFrame(response);
    }

}
