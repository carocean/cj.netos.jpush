package cj.netos.jpush.terminal;

import cj.netos.jpush.terminal.server.TcpNodeServer;
import cj.netos.jpush.terminal.server.WSNodeServer;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;

import java.io.FileNotFoundException;

@CjService(name = "terminalNode", isExoteric = true)
public class TerminalNode implements ITerminalNode {
    ITerminalServiceProvider site;
    INodeConfig nodeConfig;
    ITerminalNodeServer nodeServer;

    @Override
    public void entrypoint(String home) throws FileNotFoundException {
        site = new TerminalServiceProvider();
        nodeConfig = new NodeConfig();
        nodeConfig.load(home);

        nodeServer = createNodeServer(nodeConfig.getServerInfo());

        nodeServer.start();
    }

    private ITerminalNodeServer createNodeServer(ServerInfo serverInfo) {
        switch (serverInfo.getProtocol()) {
            case "tcp":
                return new TcpNodeServer(site);
            case "ws":
            case "wss":
                return new WSNodeServer(site);
//            case "http":
//            case "https":
//                return new HttpNetworkNodeServer(site);
            default:
                throw new EcmException(String.format("不支持的协议：%s", serverInfo.getProtocol()));
        }
    }

    class TerminalServiceProvider implements ITerminalServiceProvider {

        @Override
        public Object getService(String serviceId) {
            if ("$.terminal.server".equals(serviceId)) {
                return nodeServer;
            }
            if ("$.terminal.config".equals(serviceId)) {
                return nodeConfig;
            }
            return null;
        }
    }
}

