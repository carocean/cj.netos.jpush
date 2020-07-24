package cj.netos.jpush.pusher;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public interface IExchangeContainer {
    Channel addExchange(Connection connection, ExchangeConfig exchange) throws IOException;

    void push(JPushFrame frame) throws CircuitException;

    void setTempQueueExchange(Connection connection, TempExchangeConfig tempExchange) throws IOException;

}
