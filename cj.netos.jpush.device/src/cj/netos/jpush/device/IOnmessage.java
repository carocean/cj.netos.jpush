package cj.netos.jpush.device;


import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface IOnmessage {
    void onmessage(ILogicNetwork logicNetwork, JPushFrame frame) throws CircuitException;
}
