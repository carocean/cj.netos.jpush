package cj.netos.jpush.terminal;

import cj.netos.jpush.IPersistenceMessageService;

public interface INotificationPlugin {
    IPersistenceMessageService getPersistenceMessageService();

    void start(INodeConfig nodeConfig);

}
