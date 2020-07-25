package cj.netos.jpush.asc;

import cj.studio.ecm.net.CircuitException;

public interface ITerminalNodeContainer {
    String[] getTerminalAddressList();

    void refresh() throws CircuitException, Exception;

}
