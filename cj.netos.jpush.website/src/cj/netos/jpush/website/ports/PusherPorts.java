package cj.netos.jpush.website.ports;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.pusher.IJPusher;
import cj.netos.jpush.website.IPusherPorts;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Map;

@CjService(name = "/pusher.ports")
public class PusherPorts implements IPusherPorts {
    @CjServiceRef(refByName = "@.jpusher")
    IJPusher jpusher;

    @Override
    public void pushToPerson(ISecuritySession securitySession, String toPerson, String headline, Map<String, String> headers, Map<String, String> parameters, String content) throws CircuitException {
        headers.put("to-person", toPerson);
        headers.put("sender-person", securitySession.principal());
        headers.put("sender-device",(String) securitySession.property("device"));
        pushImpl(securitySession, headline, headers, parameters, content);
    }

    @Override
    public void pushToDevice(ISecuritySession securitySession, String toPerson, String toDevice, String headline, Map<String, String> headers, Map<String, String> parameters, String content) throws CircuitException {
        headers.put("to-person", toPerson);
        headers.put("to-device", toDevice);
        headers.put("sender-person", securitySession.principal());
        headers.put("sender-device",(String) securitySession.property("device"));
        pushImpl(securitySession, headline, headers, parameters, content);
    }

    void pushImpl(ISecuritySession securitySession, String headline, Map<String, String> headers, Map<String, String> parameters, String content) throws CircuitException {
        ByteBuf bb = Unpooled.buffer();
        if (!StringUtil.isEmpty(content)) {
            bb.writeBytes(content.getBytes());
        }
        JPushFrame frame = new JPushFrame(headline, bb);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                frame.head(entry.getKey(), entry.getValue());
            }
        }
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                frame.parameter(entry.getKey(), entry.getValue());
            }
        }
        jpusher.push(frame);
    }
}
