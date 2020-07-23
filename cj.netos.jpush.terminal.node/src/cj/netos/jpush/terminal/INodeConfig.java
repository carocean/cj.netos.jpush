package cj.netos.jpush.terminal;

import java.io.FileNotFoundException;

public interface INodeConfig {
    void load(String home) throws FileNotFoundException;

    ServerInfo getServerInfo();

    String home();

    RestFullConfig getRestFull();

}
