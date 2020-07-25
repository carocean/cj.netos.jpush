package cj.netos.jpush.asc.program;

import cj.netos.jpush.asc.ITerminalNodeContainer;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.app.GatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;
import org.apache.curator.framework.CuratorFramework;

@CjService(name = "$.cj.studio.gateway.app", isExoteric = true)
public class AppSiteProgram extends GatewayAppSiteProgram {

    @Override
    protected void onstart(Destination dest, String assembliesHome, ProgramAdapterType type) throws CircuitException {
        ITerminalNodeContainer terminalNodeContainer = (ITerminalNodeContainer) site.getService("terminalNodeContainer");
        try {
            terminalNodeContainer.refresh();
        } catch (Exception e) {
            CircuitException ce = CircuitException.search(e);
            if (ce != null) {
                throw ce;
            }
            throw new CircuitException("500", e);
        }
    }
}
