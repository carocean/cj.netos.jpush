package cj.netos.jpush.terminal.pipeline.valve;

import cj.netos.jpush.IJPushServiceProvider;
import cj.netos.jpush.IPipeline;
import cj.netos.jpush.IValve;
import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.net.CircuitException;

public class CheckSecurityValve implements IValve {
    public CheckSecurityValve(IJPushServiceProvider site) {

    }

    @Override
    public void flow(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        if (!"NETWORK/1.0".equals(frame.protocol())) {
            nextError(new CircuitException("500", "不是系统协议"), pipeline);
            return;
        }
        if (!"login".equalsIgnoreCase(frame.command()) && pipeline.endPort() == null) {
            //不是登录指令则必须已登录，否则异常
            nextError(new CircuitException("801", "没有登录"), pipeline);
            return;
        }
        pipeline.nextFlow(frame, this);
    }

    @Override
    public void nextError(Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(error, this);
    }
}
