package cj.netos.jpush.terminal;

import cj.netos.jpush.IPipeline;
import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface ITerminalCommand {
    void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException;

    String cmd();

}
