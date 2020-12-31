package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface IBuddyPusher {
    void push(JPushFrame frame, String regId) throws CircuitException;

}
