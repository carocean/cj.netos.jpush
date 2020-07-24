package cj.netos.jpush.pusher;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IServiceContainerMonitor;
import cj.studio.ecm.net.CircuitException;

import java.io.File;
import java.io.IOException;

public class DefaultJPusherMonitor implements IServiceContainerMonitor {
    @Override
    public void onBeforeRefresh(IServiceSite site) {
        IPersonFinder personFinder = (IPersonFinder) site.getService("@.finder.person");
        if (personFinder == null) {
            throw new EcmException(String.format("缺少名为：@.finder.person 的服务。该服务用于当发送目标用户队列不存在时，会在uc中心查询用户，如果存在用户则创建用户队列。必须实现：IPersonFinder接口"));
        }
        String assembliesHome = site.getProperty("home.dir");
        String configDir = String.format("%s%sjpusher", assembliesHome, File.separator);
        File configDirFile = new File((configDir));
        if (!configDirFile.exists()) {
            configDirFile.mkdirs();
        }
        String configFile = String.format("%s%sjpush.yaml", configDirFile.getAbsolutePath(), File.separator);
        File file = new File(configFile);
        if (!file.exists()) {
            throw new EcmException(String.format("推送器配置文件不存在:%s", file));
        }
        CJSystem.logging().info(getClass(), String.format("推送器配置路径:%s", file));
        IJPusher pusher = new JPusher();
        try {
            pusher.load(file, personFinder);
        } catch (CircuitException e) {
            throw new EcmException(e);
        }
        site.addService("@.jpusher", pusher);
    }

    @Override
    public void onAfterRefresh(IServiceSite site) {

    }
}
