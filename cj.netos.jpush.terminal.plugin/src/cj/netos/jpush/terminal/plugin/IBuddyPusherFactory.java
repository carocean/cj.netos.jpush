package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;

public interface IBuddyPusherFactory {
    void push(JPushFrame frame, String device);

}
