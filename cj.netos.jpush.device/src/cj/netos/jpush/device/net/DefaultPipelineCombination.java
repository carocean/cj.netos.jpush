package cj.netos.jpush.device.net;

import cj.netos.jpush.*;
import cj.netos.jpush.device.IOnerror;
import cj.netos.jpush.device.IOnevent;
import cj.netos.jpush.device.IOnmessage;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;

public class DefaultPipelineCombination implements IPipelineCombination {
    IOnmessage onmessage;
    IOnerror onerror;
    IOnevent onevent;

    public DefaultPipelineCombination(IOnmessage onmessage, IOnerror onerror, IOnevent onevent) {
        this.onmessage = onmessage;
        this.onerror = onerror;
        this.onevent = onevent;
    }

    @Override
    public void combine(IPipeline pipeline) throws CombineException {
        pipeline.append(new IValve() {
            @Override
            public void flow(JPushFrame frame, IPipeline pipeline) throws CircuitException {
                if ("NET/1.0".equals(frame.protocol())) {

                    String status = frame.head("status");
                    if (StringUtil.isEmpty(status)) {
                        status = "200";
                    }
                    if (Double.valueOf(status).intValue() >= 400) {
                        if (onerror != null) {
                            onerror.onerror(new CircuitException(status, frame.head("message")));
                        }
                    } else {
                        if (onevent != null) {
                            onevent.onevent(frame);
                        }
                    }

                    return;
                }
                if (onmessage != null) {
                    onmessage.onmessage(frame);
                }
            }


            @Override
            public void nextError(Throwable error, IPipeline pipeline) throws CircuitException {
                if (onerror != null) {
                    onerror.onerror(error);
                }
            }
        });
    }

    @Override
    public void demolish(IPipeline pipeline) {

    }
}