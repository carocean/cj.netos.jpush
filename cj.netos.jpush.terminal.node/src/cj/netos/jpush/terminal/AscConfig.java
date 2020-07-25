package cj.netos.jpush.terminal;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;

import java.util.HashMap;
import java.util.Map;

public class AscConfig {
    String namespace;
    String mountPath;
    String connectString;
    int sessionTimeoutMs;
    int connectionTimeoutMs;
    int retryIntervalMs;

    public void parse(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        namespace = (String) map.get("namespace");
        mountPath = (String) map.get("mountPath");
        if (!mountPath.startsWith("/")) {
            throw new EcmException(String.format("mountPath格式错误，没有以/开头"));
        }
        connectString = (String) map.get("connectString");
        sessionTimeoutMs = map.get("sessionTimeoutMs") == null ? 0 : (int) map.get("sessionTimeoutMs");
        connectionTimeoutMs = map.get("connectionTimeoutMs") == null ? 0 : (int) map.get("connectionTimeoutMs");
        retryIntervalMs = map.get("retryIntervalMs") == null ? 0 : (int) map.get("retryIntervalMs");
    }

    public void printLog(Class aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%smountPath=%s", indent, mountPath));
        CJSystem.logging().info(aClass, String.format("%sconnectString=%s", indent, connectString));
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(int retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }
}
