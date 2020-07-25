package cj.netos.jpush.terminal;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

class NodeConfig implements INodeConfig {
    ServerConfig serverConfig;
    private String home;
    RestFullConfig restFullConfig;
    RabbitMQConfig rabbitMQConfig;
    AscConfig ascConfig;

    @Override
    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public String home() {
        return home;
    }

    @Override
    public RestFullConfig getRestFullConfig() {
        return restFullConfig;
    }

    @Override
    public RabbitMQConfig getRabbitMQConfig() {
        return rabbitMQConfig;
    }

    @Override
    public AscConfig getAscConfig() {
        return ascConfig;
    }

    @Override
    public void load(String home) throws FileNotFoundException {
        this.home = home;
        Yaml nodeyaml = new Yaml();
        String confNodeFile = String.format("%s%sconf%snode.yaml", home, File.separator, File.separator);
        Reader reader = new FileReader(confNodeFile);
        Map<String, Object> node = nodeyaml.load(reader);
        parseServerInfo(node);
        parseRestfulConfig(node);
        parseRabbitMQConfig(node);
        parseAscConfig(node);
    }

    private void parseAscConfig(Map<String, Object> node) {
        ascConfig = new AscConfig();
        Map<String, Object> obj = (Map<String, Object>) node.get("asc");
        ascConfig.parse(obj);
    }

    private void parseRabbitMQConfig(Map<String, Object> node) {
        rabbitMQConfig = new RabbitMQConfig();
        Map<String, Object> obj = (Map<String, Object>) node.get("rabbitmq");
        rabbitMQConfig.parse(obj);
    }

    private void parseRestfulConfig(Map<String, Object> node) {
        Map<String, Object> _ports = (Map<String, Object>) node.get("restFull");
        restFullConfig = new RestFullConfig();
        restFullConfig.parse(_ports);
    }

    private void parseServerInfo(Map<String, Object> node) {
        serverConfig = new ServerConfig();
        serverConfig.parse(node);
    }

}
