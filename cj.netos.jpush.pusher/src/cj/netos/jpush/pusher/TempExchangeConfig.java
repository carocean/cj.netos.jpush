package cj.netos.jpush.pusher;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempExchangeConfig {
    String name;
    List<String> queues;
    TempBasicQosConfig TempBasicQosConfig;

    @SuppressWarnings("unchecked")
    public void parse(Map<String, Object> node) {
        name = node.get("name") == null ? "" : (String) node.get("name");
        queues = node.get("queues") == null ? new ArrayList<>() : (List<String>) node.get("queues");
        TempBasicQosConfig = new TempBasicQosConfig();
        Map<String, Object> qosConfigMap = node.get("basicQos") == null ? new HashMap<>() : (Map<String, Object>) node.get("basicQos");
        TempBasicQosConfig.parse(qosConfigMap);
    }

    public void printLog(Class<?> aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%sprefixName=%s", indent, name));
    }

    public TempBasicQosConfig getTempBasicQosConfig() {
        return TempBasicQosConfig;
    }

    public void setTempBasicQosConfig(TempBasicQosConfig tempBasicQosConfig) {
        this.TempBasicQosConfig = tempBasicQosConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getQueues() {
        return queues;
    }

    public void setQueues(List<String> queues) {
        this.queues = queues;
    }
}
