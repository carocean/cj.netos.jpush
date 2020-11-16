package cj.netos.jpush.terminal;

import java.io.FileNotFoundException;

public interface INodeConfig {
    RabbitMQConfig getRabbitMQConfig();

    AscConfig getAscConfig();

    boolean isUsableNotificationPlugin();

    String getNotificationPluginHome();

    void load(String home) throws FileNotFoundException;

    ServerConfig getServerConfig();

    String home();

    RestFullConfig getRestFullConfig();

}
