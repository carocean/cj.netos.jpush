package cj.netos.jpush.terminal;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

class NodeConfig implements INodeConfig {
    ServerInfo serverInfo;
    private String home;
    RestFullConfig dependOnPorts;
    RabbitMQConfig rabbitMQConfig;

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public String home() {
        return home;
    }

    @Override
    public RestFullConfig getRestFull() {
        return dependOnPorts;
    }

    @Override
    public RabbitMQConfig getRabbitMQConfig() {
        return rabbitMQConfig;
    }

    @Override
    public void load(String home) throws FileNotFoundException {
        this.home = home;
        Yaml nodeyaml = new Yaml();
        String confNodeFile = String.format("%s%sconf%snode.yaml", home, File.separator, File.separator);
        Reader reader = new FileReader(confNodeFile);
        Map<String, Object> node = nodeyaml.load(reader);
        parseServerInfo(node);
        parseDependOnPorts(node);
        parseRabbitMQConfig(node);
    }

    private void parseRabbitMQConfig(Map<String, Object> node) {
        rabbitMQConfig = new RabbitMQConfig();
        Map<String, Object> obj = (Map<String, Object>) node.get("rabbitmq");
        rabbitMQConfig.parse(obj);
    }

    private void parseDependOnPorts(Map<String, Object> node) {
        Map<String, Object> _ports = (Map<String, Object>) node.get("restFull");
        dependOnPorts = new RestFullConfig();
        dependOnPorts.parse(_ports);
    }

    private void parseServerInfo(Map<String, Object> node) {
        serverInfo = new ServerInfo();
        serverInfo.parse(node);
    }

}
