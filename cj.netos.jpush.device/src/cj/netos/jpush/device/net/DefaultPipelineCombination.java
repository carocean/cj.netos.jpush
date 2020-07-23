package cj.netos.jpush.device.net;

import cj.netos.jpush.*;
import cj.netos.jpush.device.IOnerror;
import cj.netos.jpush.device.IOnmessage;
import cj.studio.ecm.net.CircuitException;

public class DefaultPipelineCombination implements IPipelineCombination {
    IOnmessage onmessage;
    IOnerror onerror;

    public DefaultPipelineCombination(IOnmessage onmessage, IOnerror onerror) {
        this.onmessage = onmessage;
        this.onerror = onerror;
    }

    @Override
    public void combine(IPipeline pipeline) throws CombineException {
        pipeline.append(new IValve() {
            @Override
            public void flow(JPushFrame frame, IPipeline pipeline) throws CircuitException {
                if ("NETWORK/1.0".equals(frame.protocol())) {
                    return;
                }

            }


            @Override
            public void nextError( Throwable error, IPipeline pipeline) throws CircuitException {
                //不支持
            }
        });
    }

    @Override
    public void demolish(IPipeline pipeline) {

    }
}