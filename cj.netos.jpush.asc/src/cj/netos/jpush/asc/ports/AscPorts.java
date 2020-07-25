package cj.netos.jpush.asc.ports;

import cj.netos.jpush.asc.IAscPorts;
import cj.netos.jpush.asc.ITerminalNodeContainer;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

@CjService(name = "/center.ports")
public class AscPorts implements IAscPorts {
    @CjServiceRef
    ITerminalNodeContainer terminalNodeContainer;

    @Override
    public String[] getTerminalAddressList(ISecuritySession securitySession) throws CircuitException {
        return terminalNodeContainer.getTerminalAddressList();
    }
}
