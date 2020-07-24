package cj.netos.jpush.pusher;

import cj.studio.ecm.CJSystem;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPusherConfig {
    String host;
    int port;
    String virtualHost;
    String user;
    String pwd;
    boolean automaticRecoveryEnabled;
    int requestedHeartbeat;
    int connectionTimeout;
    int workPoolTimeout;
    Map<String, ExchangeConfig> exchanges;
    PersonQueueConfig personQueueConfig;
    TempExchangeConfig tempExchangeConfig;

    public static JPusherConfig load(Reader reader) {
        JPusherConfig config = new JPusherConfig();
        Yaml nodeyaml = new Yaml();
        Map<String, Object> node = nodeyaml.load(reader);
        config.parse(node);
        return config;
    }

    private void parse(Map<String, Object> node) {
        this.exchanges = new HashMap<>();
        host = node.get("host") == null ? "" : node.get("host") + "";
        port = node.get("port") == null ? 80 : (int) node.get("port");
        virtualHost = node.get("virtualHost") == null ? "" : node.get("virtualHost") + "";
        user = node.get("user") == null ? "" : node.get("user") + "";
        pwd = node.get("pwd") == null ? "" : node.get("pwd") + "";
        automaticRecoveryEnabled = node.get("automaticRecoveryEnabled") == null ? false : (boolean) node.get("automaticRecoveryEnabled");
        requestedHeartbeat = node.get("requestedHeartbeat") == null ? 0 : (int) node.get("requestedHeartbeat");
        connectionTimeout = node.get("connectionTimeout") == null ? 0 : (int) node.get("connectionTimeout");
        workPoolTimeout = node.get("workPoolTimeout") == null ? 0 : (int) node.get("workPoolTimeout");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> exchangesList = (List<Map<String, Object>>) node.get("exchanges");
        for (Map<String, Object> exchangeMap : exchangesList) {
            ExchangeConfig exchange = new ExchangeConfig();
            exchange.parse(exchangeMap);
            if (this.exchanges.containsKey(exchange.getName())) {
                CJSystem.logging().warn(getClass(), String.format("交换器名字冲突:%s", exchange.getName()));
                continue;
            }
            this.exchanges.put(exchange.getName(), exchange);
        }
        this.personQueueConfig = new PersonQueueConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> personQConfigMap = (Map<String, Object>) node.get("personQueue");
        this.personQueueConfig.parse(personQConfigMap);

        this.tempExchangeConfig = new TempExchangeConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> tempExchangeConfigMap = (Map<String, Object>) node.get("tempExchange");
        this.tempExchangeConfig.parse(tempExchangeConfigMap);
    }

    public TempExchangeConfig getTempExchangeConfig() {
        return tempExchangeConfig;
    }

    public void setTempExchangeConfig(TempExchangeConfig tempExchangeConfig) {
        this.tempExchangeConfig = tempExchangeConfig;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public boolean isAutomaticRecoveryEnabled() {
        return automaticRecoveryEnabled;
    }

    public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    }

    public int getRequestedHeartbeat() {
        return requestedHeartbeat;
    }

    public void setRequestedHeartbeat(int requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getWorkPoolTimeout() {
        return workPoolTimeout;
    }

    public void setWorkPoolTimeout(int workPoolTimeout) {
        this.workPoolTimeout = workPoolTimeout;
    }

    public Map<String, ExchangeConfig> getExchanges() {
        return exchanges;
    }

    public PersonQueueConfig getPersonQueueConfig() {
        return personQueueConfig;
    }

    public void setPersonQueueConfig(PersonQueueConfig personQueueConfig) {
        this.personQueueConfig = personQueueConfig;
    }

    public void printLog() {
        CJSystem.logging().info(getClass(), String.format("host=%s", host));
        CJSystem.logging().info(getClass(), String.format("port=%s", port));
        CJSystem.logging().info(getClass(), String.format("virtualHost=%s", virtualHost));
        CJSystem.logging().info(getClass(), String.format("user=%s", user));
        CJSystem.logging().info(getClass(), String.format("pwd=%s", pwd));
        CJSystem.logging().info(getClass(), String.format("automaticRecoveryEnabled=%s", this.automaticRecoveryEnabled));
        CJSystem.logging().info(getClass(), String.format("connectionTimeout=%s", this.connectionTimeout));
        CJSystem.logging().info(getClass(), String.format("requestedHeartbeat=%s", this.requestedHeartbeat));
        CJSystem.logging().info(getClass(), String.format("workPoolTimeout=%s", this.workPoolTimeout));
        CJSystem.logging().info(getClass(), String.format("personQueue:"));
        personQueueConfig.printLog(getClass(), "\t");
        CJSystem.logging().info(getClass(), String.format("tempExchange:"));
        tempExchangeConfig.printLog(getClass(), "\t");
        CJSystem.logging().info(getClass(), String.format("exchanges:"));
        for (Map.Entry<String, ExchangeConfig> entry : this.exchanges.entrySet()) {
            CJSystem.logging().info(getClass(), String.format("\t%s:", entry.getKey()));
            ExchangeConfig exchange = entry.getValue();
            exchange.printLog(getClass(), "\t\t");
        }
    }
}
