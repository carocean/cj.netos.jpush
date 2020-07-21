package cj.netos.jpush.device;


import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface ILogicNetwork {
    String getNetwork();

    void leave() throws CircuitException;

    void ls(String memberIn) throws CircuitException;

    void send(JPushFrame frame) throws CircuitException;

    void onmessage(IOnmessage onmessage);


}
