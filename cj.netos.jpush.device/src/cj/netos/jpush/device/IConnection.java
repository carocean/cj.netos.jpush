package cj.netos.jpush.device;


import cj.netos.jpush.JPushFrame;

import java.util.Map;

public interface IConnection {
    String getHost();

    String getProtocol();

    int getPort();

    void connect(String protocol, String ip, int port, Map<String, String> props);

    void close();

    void send(JPushFrame frame);

    boolean isConnected();

    void onclose();

    void onopen();

}
