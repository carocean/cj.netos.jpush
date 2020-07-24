package cj.netos.jpush.pusher;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class JPusher implements IJPusher {
    JPusherConfig config;
    IExchangeContainer exchangeContainer;
    Connection connection;

    @Override
    public void load(File configFile, IPersonFinder personFinder) throws CircuitException {
        _loadConfig(configFile);
        exchangeContainer = new ExchangeContainer(personFinder, config);

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

            Map<String, ExchangeConfig> exchangeConfigs = config.getExchanges();
            for (Map.Entry<String, ExchangeConfig> entry : exchangeConfigs.entrySet()) {
                ExchangeConfig exchange = entry.getValue();
                exchangeContainer.addExchange(connection, exchange);
            }
            exchangeContainer.setTempQueueExchange(connection, config.getTempExchangeConfig());

            CJSystem.logging().info(getClass(), "连接mq成功，配置如下:");
            config.printLog();
        } catch (TimeoutException e) {
            throw new CircuitException("500", e);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
    }

    private void _loadConfig(File configFile) throws CircuitException {
        Reader reader = null;
        try {
            reader = new FileReader(configFile);
            config = JPusherConfig.load(reader);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void push(JPushFrame frame) throws CircuitException {
        String toPerson = frame.head("to-person");
        if (StringUtil.isEmpty(toPerson)) {
            throw new CircuitException("404", String.format("缺少发送目标to-person"));
        }
        String senderPerson = frame.head("sender-person");
        if (StringUtil.isEmpty(senderPerson)) {
            throw new CircuitException("404", String.format("缺少发送来源sender-person"));
        }
        exchangeContainer.push(frame);
    }
}
