package cj.netos.jpush.device;


import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

/**
 * 系统事件，如上线下线
 */
public interface IOnevent {
    void onevent(JPushFrame frame) throws CircuitException;
}
