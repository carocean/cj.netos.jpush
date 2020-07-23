package cj.netos.jpush.device;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.device.net.TcpConnection;
import cj.netos.jpush.device.net.WSConnection;
import cj.studio.ecm.EcmException;
import cj.ultimate.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class Device implements IDevice {
    IConnection connection;
    boolean isAuthed = false;

    private Device() {
    }

    private static void parseProps(String queryString, Map<String, String> props) {
        String[] arr = queryString.split("&");
        for (String pair : arr) {
            if (StringUtil.isEmpty(pair)) {
                continue;
            }
            String[] e = pair.split("=");
            String key = e[0];
            String v = "";
            if (e.length > 1) {
                v = e[1];
            }
            props.put(key, v);
        }
    }

    public static IDevice connect(String url, IOnopen onopen, IOnclose onclose, IOnerror onerror, IOnevent onevent, IOnmessage onmessage) {
        int pos = url.indexOf("://");
        if (pos < 0) {
            throw new EcmException("地址格式错误:" + url);
        }
        String protocol = url.substring(0, pos);
        String remain = url.substring(pos + "://".length());
        pos = remain.indexOf("?");
        Map<String, String> props = new HashMap<>();
        String conn = "";
        if (pos < 1) {
            conn = remain;
        } else {
            conn = remain.substring(0, pos);
            String qs = remain.substring(pos + 1);
            parseProps(qs, props);
        }
        String domain = "";
        String relurl = "";
        pos = conn.indexOf("/");
        if (pos < 1) {
            domain = conn;
        } else {
            domain = conn.substring(0, pos);
            relurl = conn.substring(pos + 1);
        }
        String ip = "";
        int port = 0;
        pos = domain.indexOf(":");
        if (pos < 0) {
            ip = domain;
            port = 80;
        } else {
            ip = domain.substring(0, pos);
            port = Integer.valueOf(domain.substring(pos + 1));
        }
        IConnection connection = null;
        switch (protocol) {
            case "tcp":
                connection = new TcpConnection(onopen, onclose, onerror, onevent, onmessage);
                connection.connect(protocol, ip, port, props);
                break;
            case "ws":
            case "wss":
                if (!StringUtil.isEmpty(relurl) && !props.containsKey("wspath")) {
                    props.put("wspath", relurl);
                }
                connection = new WSConnection(onopen, onclose, onerror, onevent, onmessage);
                connection.connect(protocol, ip, port, props);
                break;
            default:
                throw new EcmException("不支持的协议:" + protocol);
        }
        Device device = new Device();
        device.connection = connection;
        return device;
    }

    @Override
    public void login(String token) {
        JPushFrame frame = new JPushFrame("login / NET/1.0");
        frame.head("accessToken", token);
        connection.send(frame);
    }

    @Override
    public void lsInfo() {
        JPushFrame frame = new JPushFrame("ls / NET/1.0");
        connection.send(frame);
    }

    @Override
    public void resume() {
        JPushFrame frame = new JPushFrame("resume / NET/1.0");
        connection.send(frame);
    }

    @Override
    public void pause() {
        JPushFrame frame = new JPushFrame("pause / NET/1.0");
        connection.send(frame);
    }

    @Override
    public void adminLs() {
        JPushFrame frame = new JPushFrame("adminLs / NET/1.0");
        connection.send(frame);
    }

    @Override
    public void adminView(String u) {
        JPushFrame frame = new JPushFrame("adminView / NET/1.0");
        frame.head("person", u);
        connection.send(frame);
    }

    @Override
    public void close() {
        connection.close();
    }

}
