package cj.netos.jpush.terminal;


import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public interface IPersonConsumerQueue {
    void handleDelivery(AMQP.Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws CircuitException;

    void handleShutdownSignal(String consumerTag, ShutdownSignalException sig);

}
