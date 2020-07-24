package cj.netos.jpush.pusher;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

import java.io.File;

public interface IJPusher {
    void push(JPushFrame frame) throws CircuitException;

    void load(File configFile, IPersonFinder personFinder) throws CircuitException;

}
