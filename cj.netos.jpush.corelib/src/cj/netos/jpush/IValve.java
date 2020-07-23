package cj.netos.jpush;

import cj.studio.ecm.net.CircuitException;

public interface IValve {
	void flow(JPushFrame frame, IPipeline pipeline)throws CircuitException;

    void nextError(Throwable error, IPipeline pipeline)throws CircuitException;
}
