package cj.netos.jpush.pusher;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.util.Encript;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExchangeContainer implements IExchangeContainer, ReturnListener {
    List<Channel> channels;
    Map<Channel, ExchangeConfig> indexMap;
    IPersonFinder personFinder;
    JPusherConfig config;
    Channel tempExchangeChannel;

    public ExchangeContainer(IPersonFinder personFinder, JPusherConfig config) {
        channels = new ArrayList<>();
        indexMap = new HashMap<>();
        this.personFinder = personFinder;
        this.config = config;
    }

    @Override
    public Channel addExchange(Connection connection, ExchangeConfig exchange) throws IOException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange.getName(), exchange.getType(), exchange.isDurable(), exchange.isAutoDelete(), exchange.isInternal(), exchange.getArguments());
        channels.add(channel);
        indexMap.put(channel, exchange);
        channel.addReturnListener(this);
        return channel;
    }

    @Override
    public void setTempQueueExchange(Connection connection, TempExchangeConfig config) throws IOException {
        tempExchangeChannel = connection.createChannel();
        tempExchangeChannel.exchangeDeclare(config.getName(), "direct", true, false, false, null);
        List<String> queues = config.queues;
        TempBasicQosConfig basicQosConfig = config.getTempBasicQosConfig();
        for (int i = 0; i < queues.size(); i++) {
            String queue = queues.get(i);
            String routingKey = String.format("routingKey.%s", i);
            tempExchangeChannel.queueDeclare(queue, true, false, false, null);
            tempExchangeChannel.queueBind(queue, config.getName(), routingKey);
            Channel consumeChannel = connection.createChannel();
            consumeChannel.queueBind(queue, config.getName(), routingKey, null);
            consumeChannel.basicQos(basicQosConfig.getPrefetchSize(), basicQosConfig.getPrefetchCount(), basicQosConfig.isGlobal());
            consumeChannel.basicConsume(queue, false, new DefaultTempConsumer(consumeChannel));
        }

    }

    @Override
    public void push(JPushFrame frame) throws CircuitException {
        if (channels.isEmpty()) {
            CJSystem.logging().warn(getClass(), String.format("没有交换器，消息被丢弃。%s", frame));
            return;
        }
        String toPerson = frame.head("to-person");
        String routingKey = String.format("person.%s", toPerson);
        Channel channel = selectChannel(frame);
        ExchangeConfig exchange = indexMap.get(channel);

        Map<String, Object> headers = new HashMap<>();
        for (String key : frame.enumHeadName()) {
            if ("url".equals(key)) {
                continue;
            }
            headers.put(key, frame.head(key));
        }
        String url = frame.retrieveUrl();
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .type(url)
                .headers(headers).build();
        byte[] body = frame.content().readFully();
        try {
            channel.basicPublish(exchange.getName(), routingKey, true, properties, body);
            frame.dispose();
        } catch (IOException e) {
            CJSystem.logging().error(getClass(), String.format("发送消息失败。%s", frame));
        }
    }

    private Channel selectChannel(JPushFrame frame) {
        if (channels.size() == 1) {
            return channels.get(0);
        }
        String md5 = Encript.md5(String.format("%s%s", frame.hashCode(), System.currentTimeMillis()));
        int index = Math.abs(md5.hashCode()) % channels.size();
        return channels.get(index);
    }

    @Override
    public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
//        CJSystem.logging().info(getClass(), String.format("----%s", replyCode));
        if (personFinder != null && replyCode == 312 && "NO_ROUTE".equals(replyText)) {
            LongString toPerson = (LongString) properties.getHeaders().get("to-person");
            PersonInfo obj = null;
            try {
                obj = personFinder.find(toPerson.toString());
            } catch (Throwable e) {
                CJSystem.logging().warn(getClass(), String.format("\t查询用户失败:%s", e));
                return;
            }
            try {
                if (obj != null) {
                    TempExchangeConfig tempExchangeConfig = config.tempExchangeConfig;
                    String md5 = Encript.md5(String.format("%s%s", routingKey, System.currentTimeMillis()));
                    int index = Math.abs(md5.hashCode()) % tempExchangeConfig.getQueues().size();
                    String directToRoutingKey = String.format("routingKey.%s", index);
                    tempExchangeChannel.basicPublish(tempExchangeConfig.getName(), directToRoutingKey, true, properties, body);
                    return;
                }
            } catch (Throwable e) {
                CJSystem.logging().warn(getClass(), String.format("\t构建用户队列失败:%s", e));
                return;
            }
        }
        CJSystem.logging().warn(getClass(), String.format("消息被丢弃，replyCode=%s, replyText=%s 请检查routingKey是否与交易节点注册的routingKey对应，详情:", replyCode, replyText));
        CJSystem.logging().warn(getClass(), String.format("\texchange:%s", exchange));
        CJSystem.logging().warn(getClass(), String.format("\troutingKey:%s", routingKey));
        CJSystem.logging().warn(getClass(), String.format("\tbody:\r\n%s", new String(body)));
    }


    private class DefaultTempConsumer extends DefaultConsumer {

        public DefaultTempConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            CJSystem.logging().error(getClass(), sig);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            try {
                LongString person = (LongString) properties.getHeaders().get("to-person");
                String routingKey = String.format("person.%s", person);
                for (Map.Entry<Channel, ExchangeConfig> entry : indexMap.entrySet()) {
                    buildPersonQueue(person.toString(), entry.getKey(), entry.getValue(), routingKey);
                }

                String md5 = Encript.md5(String.format("%s%s", consumerTag, System.currentTimeMillis()));
                int index = Math.abs(md5.hashCode()) % channels.size();
                Channel channel = channels.get(index);
                ExchangeConfig config = indexMap.get(channel);
                channel.basicPublish(config.getName(), routingKey, true, properties, body);
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } catch (Throwable throwable) {
                CJSystem.logging().error(getClass(), throwable);
                //默认异常则会消费掉消息（即丢掉）
                getChannel().basicReject(envelope.getDeliveryTag(), false);
            }
        }

        private void buildPersonQueue(String person, Channel channel, ExchangeConfig exchangeConfig, String routingKey) throws IOException {
            PersonQueueConfig queueConfig = config.getPersonQueueConfig();
            String queueName = String.format("%s%s", queueConfig.prefixName, person);
            channel.exchangeDeclarePassive(exchangeConfig.name);
            channel.queueDeclare(queueName, queueConfig.isDurable(), queueConfig.isExclusive(), queueConfig.isAutoDelete(), queueConfig.getArguments());

            channel.queueBind(queueName, exchangeConfig.name, routingKey);
        }
    }
}
