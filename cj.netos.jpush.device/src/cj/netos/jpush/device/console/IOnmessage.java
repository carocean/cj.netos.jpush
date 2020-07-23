package cj.netos.jpush.device.console;


import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface IOnmessage {
    void onmessage(JPushFrame frame) throws CircuitException;
}
