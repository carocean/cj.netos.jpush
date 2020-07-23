package cj.netos.jpush.terminal;


import cj.netos.jpush.EndPort;
import cj.netos.jpush.IJPushServiceProvider;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.List;

public interface IRabbitMQConsumer {
    boolean isOpened();


    RabbitMQConfig config();


    Channel open(IJPushServiceProvider site) throws CircuitException;


    void close() throws CircuitException;


    void bindEndPort(PersonEndPorts personEndPorts) throws IOException;

    void unbindEndPort(PersonEndPorts personEndPorts) throws IOException;

    void consumePersonQueue(PersonEndPorts personEndPorts) throws CircuitException;

    void cancelConsumePersonQueue(PersonEndPorts personEndPorts) throws CircuitException;


}
