package cj.netos.jpush.pusher;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface IJPusher {
    void push(JPushFrame frame) throws CircuitException;

}
