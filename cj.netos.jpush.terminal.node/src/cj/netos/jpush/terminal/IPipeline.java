package cj.netos.jpush.terminal;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IDisposable;

public interface IPipeline extends IDisposable {

    EndPortInfo endPort();

    void endPort(EndPortInfo endPort);

    /**
     * 管道的附件，也可能空
     *
     * @return
     */
    Object attachment();

    /**
     * 设置附件
     *
     * @param attachment
     */
    void attachment(Object attachment);

    void append(IValve valve);

    void input(JPushFrame frame) throws CircuitException;

    void nextFlow(JPushFrame frame, IValve current) throws CircuitException;

    void nextError(JPushFrame frame, Throwable error, IValve current) throws CircuitException;

    void remove(IValve valve);

    ITerminalServiceProvider site();

    void error(JPushFrame frame, Throwable e) throws CircuitException;

    boolean isEmpty();

}
