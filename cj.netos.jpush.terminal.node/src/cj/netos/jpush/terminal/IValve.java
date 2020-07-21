package cj.netos.jpush.terminal;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public interface IValve {
	void flow(JPushFrame frame, IPipeline pipeline)throws CircuitException;

    void nextError(JPushFrame frame, Throwable error, IPipeline pipeline)throws CircuitException;
}
