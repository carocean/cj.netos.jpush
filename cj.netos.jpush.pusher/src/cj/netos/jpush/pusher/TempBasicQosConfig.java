package cj.netos.jpush.pusher;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TempBasicQosConfig {
    int prefetchSize;
    int prefetchCount;
    boolean global;

    @SuppressWarnings("unchecked")
    public void parse(Map<String, Object> node) {
        prefetchSize = node.get("prefetchSize") == null ? 0 : (int) node.get("prefetchSize");
        prefetchCount = node.get("prefetchCount") == null ? 0 : (int) node.get("prefetchCount");
        global = node.get("prefetchSize") == null ? false : (boolean) node.get("global");

    }

    public void printLog(Class<?> aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%sprefetchCount=%s", indent, prefetchCount));
    }

    public int getPrefetchSize() {
        return prefetchSize;
    }

    public void setPrefetchSize(int prefetchSize) {
        this.prefetchSize = prefetchSize;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
