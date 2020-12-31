package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface IBuddyPusherFactory {
    void push(JPushFrame frame, String device) throws CircuitException;

}
