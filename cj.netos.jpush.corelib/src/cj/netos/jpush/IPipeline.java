package cj.netos.jpush;

import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IDisposable;

public interface IPipeline extends IDisposable {

    EndPort endPort();

    void endPort(EndPort endPort);

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

    void nextError(Throwable error, IValve current) throws CircuitException;

    void remove(IValve valve);

    IJPushServiceProvider site();

    void error(Throwable e) throws CircuitException;

    boolean isEmpty();

}
