package cj.netos.jpush;

import cj.studio.ecm.net.CircuitException;

public interface IPersistenceMessageService {

    void writeFrame(JPushFrame frame, String person, String nickName) throws CircuitException;

    void downstream(EndPort endPort) throws CircuitException;

    /**
     * 检查用户用过的设备，如果不存在或存在且不一样就更新设备，供writeFrame发通知时使用
     * @param endPort
     */
    void checkAndUpdateBuddyDevice(EndPort endPort);

    void removeBuddyDevice(EndPort theEndPort);

}
