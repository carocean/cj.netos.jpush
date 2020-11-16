package cj.netos.jpush.terminal;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IJPushServiceProvider;
import cj.netos.jpush.IPersistenceMessageService;
import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import com.rabbitmq.client.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumer implements IRabbitMQConsumer {
    RabbitMQConfig config;
    Channel channel;
    Connection connection;
    IPersistenceMessageService persistenceMessageService;

    @Override
    public boolean isOpened() {
        return (channel != null && channel.isOpen());
    }

    @Override
    public RabbitMQConfig config() {
        return config;
    }

    @Override
    public Channel open(IJPushServiceProvider site) throws CircuitException {
        config = ((NodeConfig) site.getService("$.terminal.config")).rabbitMQConfig;
        persistenceMessageService = (IPersistenceMessageService) site.getService("$.plugin.persistenceMessageService");
        String host = config.getHost();
        int port = config.getPort();
        String virtualHost = config.getVirtualHost();
        String user = config.getUser();
        String pwd = config.getPwd();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(pwd);
            connectionFactory.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());
            if (config.getRequestedHeartbeat() > 0) {
                connectionFactory.setRequestedHeartbeat(config.getRequestedHeartbeat());
            }
            if (config.getConnectionTimeout() > 0) {
                connectionFactory.setConnectionTimeout(config.getConnectionTimeout());
            }
            if (config.getWorkPoolTimeout() > 0) {
                connectionFactory.setWorkPoolTimeout(config.getWorkPoolTimeout());
            }

            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            //设置客户端最多接收未被ack的消息的个数
            BasicQosConfig basicQosConfig = config.getBasicQos();
            channel.basicQos(basicQosConfig.getPrefetchSize(), basicQosConfig.getPrefetchCount(), basicQosConfig.isGlobal());
            //4 声明交换机和队列，然后进行绑定设置路由Key
            for (String exchange : config.getExchanges()) {
                Map<String, ExchangeConfig> exchangeConfigMap = config.getAutoCreateExchanges();
                if (exchangeConfigMap.containsKey(exchange)) {
                    ExchangeConfig exchangeConfig = exchangeConfigMap.get(exchange);
                    channel.exchangeDeclare(exchange, exchangeConfig.getType(), exchangeConfig.isDurable(), exchangeConfig.isAutoDelete(), exchangeConfig.isInternal(), exchangeConfig.getArguments());
                    continue;
                }
                //如果已存在则使用该方法，而使用exchangeDeclare这个方法，如果存在则直接用不存在则会自建。
                //经验证，一个channel上可以定义多个交换器，但如果一个交换器已被定义，则不可再通过exchangeDeclare修改，那怕是变更属性都不可
                try {
                    channel.exchangeDeclarePassive(exchange);
                } catch (IOException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ShutdownSignalException) {
                        ShutdownSignalException shutdownSignalException = (ShutdownSignalException) cause;
                        AMQP.Channel.Close method = (AMQP.Channel.Close) shutdownSignalException.getReason();
                        int replyCode = method.getReplyCode();
                        if (replyCode == 404) {
                            throw new CircuitException("404", String.format("RabbitMQ上不存在交换器 %s，请先启动消息生产者节点以生成Exchange，或者按生产者节点的交换机配置手工在rabbitmq上配置该交换器", exchange));
                        }
                    }
                    throw new CircuitException("500", e);
                }
            }
            CJSystem.logging().info(getClass(), "连接mq成功，配置如下:");
            config.printLog();
            return channel;
        } catch (TimeoutException e) {
            throw new CircuitException("500", e);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
    }

    @Override
    public void close() throws CircuitException {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                CJSystem.logging().error(getClass(), e);
            } catch (TimeoutException e) {
                CJSystem.logging().error(getClass(), e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                CJSystem.logging().error(getClass(), e);
            }
        }
        CJSystem.logging().info(getClass(), String.format("已断开mq"));
    }

    @Override
    public void bindEndPort(PersonEndPorts personEndPorts) throws IOException {
        PersonQueueConfig queueConfig = config.getPersonQueueConfig();
        String queueName = String.format("%s%s", queueConfig.prefixName, personEndPorts.getPerson());
        channel.queueDeclare(queueName, queueConfig.isDurable(), queueConfig.isExclusive(), queueConfig.isAutoDelete(), queueConfig.getArguments());
        List<String> exchanges = config.getExchanges();
        for (String exchange : exchanges) {
            channel.exchangeDeclarePassive(exchange);
            String personRoutingKey = String.format("person.%s", personEndPorts.getPerson());
            channel.queueBind(queueName, exchange, personRoutingKey);
            for (EndPort port : personEndPorts.endPorts()) {
                if (port == null) {
                    continue;
                }
                String deviceRoutingKey = String.format("device.%s", port.getDevice());
                channel.queueBind(queueName, exchange, deviceRoutingKey);
            }
        }
    }

    @Override
    public void unbindPerson(PersonEndPorts personEndPorts) throws IOException {
        PersonQueueConfig queueConfig = config.getPersonQueueConfig();
        String queueName = String.format("%s%s", queueConfig.prefixName, personEndPorts.getPerson());
        List<String> exchanges = config.getExchanges();
        for (String exchange : exchanges) {
            channel.exchangeDeclarePassive(exchange);
            String personRoutingKey = String.format("person.%s", personEndPorts.getPerson());
            channel.queueUnbind(queueName, exchange, personRoutingKey);
            for (EndPort port : personEndPorts.endPorts()) {
                if (port == null) {
                    continue;
                }
                String deviceRoutingKey = String.format("device.%s", port.getDevice());
                channel.queueUnbind(queueName, exchange, deviceRoutingKey);
            }
        }
    }

    @Override
    public void unbindEndPort(EndPort port) throws IOException {
        PersonQueueConfig queueConfig = config.getPersonQueueConfig();
        String queueName = String.format("%s%s", queueConfig.prefixName, port.getPerson());
        List<String> exchanges = config.getExchanges();
        for (String exchange : exchanges) {
            channel.exchangeDeclarePassive(exchange);
            String deviceRoutingKey = String.format("device.%s", port.getDevice());
            channel.queueUnbind(queueName, exchange, deviceRoutingKey);
        }
    }

    @Override
    public void consumePersonQueue(PersonEndPorts personEndPorts) throws CircuitException {
        PersonQueueConfig queueConfig = config.getPersonQueueConfig();
        String queueName = String.format("%s%s", queueConfig.prefixName, personEndPorts.getPerson());
        try {
//            long count = channel.consumerCount(queueName);
            String customTag = channel.basicConsume(queueName, config.isAutoAck(), new CLAFConsumerDelivery(channel, persistenceMessageService, personEndPorts));
            personEndPorts.setConsumerTag(customTag);
            personEndPorts.setConsumed(true);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
    }

    @Override
    public void stopConsumePersonQueue(PersonEndPorts personEndPorts) throws CircuitException {
        try {
            channel.basicCancel(personEndPorts.getConsumerTag());
        } catch (IOException e) {
            CJSystem.logging().error(getClass(), e.getMessage());
            return;
        } finally {
            personEndPorts.setConsumed(false);
        }
    }

    class CLAFConsumerDelivery extends DefaultConsumer {
        PersonEndPorts personEndPorts;
        IPersistenceMessageService persistenceMessageService;

        public CLAFConsumerDelivery(Channel channel, IPersistenceMessageService persistenceMessageService, PersonEndPorts personEndPorts) {
            super(channel);
            this.persistenceMessageService = persistenceMessageService;
            this.personEndPorts = personEndPorts;
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            CJSystem.logging().error(getClass(), sig);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            try {
                String url = properties.getType();
                if (StringUtil.isEmpty(url)) {
                    //丢弃
                    getChannel().basicReject(envelope.getDeliveryTag(), false);
                    CJSystem.logging().warn(getClass(), String.format("路由:%s 缺少type，已丢弃", envelope.getRoutingKey()));
                    return;
                }
                LongString cmd = (LongString) properties.getHeaders().get("command");
                if (cmd == null) {
                    //丢弃
                    getChannel().basicReject(envelope.getDeliveryTag(), false);
                    CJSystem.logging().warn(getClass(), String.format("路由:%s 消息头中缺少command，已丢弃", envelope.getRoutingKey()));
                    return;
                }
                LongString protocol = (LongString) properties.getHeaders().get("protocol");
                if (protocol == null) {
                    //丢弃
                    getChannel().basicReject(envelope.getDeliveryTag(), false);
                    CJSystem.logging().warn(getClass(), String.format("路由:%s 消息头中缺少protocol，已丢弃", envelope.getRoutingKey()));
                    return;
                }
                if ("NET/1.0".equalsIgnoreCase(protocol.toString())) {
                    //丢弃
                    getChannel().basicReject(envelope.getDeliveryTag(), false);
                    CJSystem.logging().warn(getClass(), String.format("路由:%s 消息的protocol不能是系统协议：NET/1.0", envelope.getRoutingKey()));
                    return;
                }
                Map<String, Object> headers = properties.getHeaders();
                ByteBuf bb = Unpooled.buffer();
                if (body != null && body.length > 0) {
                    bb.writeBytes(body);
                }
                JPushFrame frame = new JPushFrame(String.format("%s %s %s", cmd, url, protocol), bb);
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    frame.head(entry.getKey(), entry.getValue() + "");
                }
                if (persistenceMessageService != null && personEndPorts.isEmpty()) {//只要开启了通知模式且用户没一个终结点在线，一律采用插件缓冲消息，只是device不是合作推送的厂商的不发通知而已，它会在用户上线时一次性下行
                    persistenceMessageService.writeFrame(frame.copy(), personEndPorts.getPerson(), personEndPorts.getNickName());
                } else {
                    for (EndPort endPort : personEndPorts.endPorts()) {
                        endPort.writeFrame(frame.copy());
                        if (persistenceMessageService != null) {
                            persistenceMessageService.checkAndUpdateBuddyDevice(endPort);
                        }
                    }
                }
                frame.dispose();
                channel.basicAck(envelope.getDeliveryTag(), false);
                //如果发生错误则会导致channel并闭，因此捕获
            } catch (Throwable throwable) {
                CJSystem.logging().error(getClass(), throwable);
                //出错之后重试
                getChannel().basicReject(envelope.getDeliveryTag(), true);
            }
        }
    }
}
