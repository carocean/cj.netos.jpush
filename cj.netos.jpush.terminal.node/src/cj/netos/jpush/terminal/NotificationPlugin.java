package cj.netos.jpush.terminal;

import cj.netos.jpush.IPersistenceMessageService;
import cj.studio.ecm.Assembly;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IAssembly;
import cj.studio.ecm.IWorkbin;

import java.io.File;
import java.io.FileFilter;

public class NotificationPlugin implements INotificationPlugin, FileFilter {
    IPersistenceMessageService persistenceMessageService;

    @Override
    public IPersistenceMessageService getPersistenceMessageService() {
        return persistenceMessageService;
    }

    @Override
    public void start(INodeConfig nodeConfig) {
        File dir = new File(nodeConfig.getNotificationPluginHome());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles(this);
        if (files.length == 0) {
            throw new EcmException("插件不存在");
        }
        if (files.length > 1) {
            throw new EcmException("发现多个插件程序集");
        }
        File file = files[0];
        IAssembly assembly = Assembly.loadAssembly(file.getAbsolutePath(), this.getClass().getClassLoader());
        assembly.start();
        IWorkbin workbin = assembly.workbin();
        persistenceMessageService = (IPersistenceMessageService) workbin.part("persistenceMessageService");
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.isFile() && pathname.getAbsolutePath().endsWith(".jar");
    }
}
