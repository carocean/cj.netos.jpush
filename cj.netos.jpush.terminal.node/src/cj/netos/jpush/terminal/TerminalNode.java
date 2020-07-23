package cj.netos.jpush.terminal;

import cj.netos.jpush.IJPushServiceProvider;
import cj.netos.jpush.terminal.server.TcpNodeServer;
import cj.netos.jpush.terminal.server.WSNodeServer;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.ChannelFuture;
import okhttp3.OkHttpClient;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

@CjService(name = "terminalNode", isExoteric = true)
public class TerminalNode implements ITerminalNode {
    IJPushServiceProvider site;
    INodeConfig nodeConfig;
    ITerminalNodeServer nodeServer;
    OkHttpClient restfull;
    IEndPortContainer endPortContainer;
    IRabbitMQConsumer rabbitMQConsumer;

    @Override
    public void entrypoint(String home) throws FileNotFoundException {
        site = new TerminalServiceProvider();
        nodeConfig = new NodeConfig();
        nodeConfig.load(home);
        buildOkHttpClient();

        nodeServer = createNodeServer(nodeConfig.getServerInfo());

        rabbitMQConsumer = new RabbitMQConsumer();

        endPortContainer = createEndPortContainer();

        ChannelFuture future=nodeServer.start();

        try {
            rabbitMQConsumer.open(site);
            future.sync();
        } catch (CircuitException e) {
            CJSystem.logging().error(getClass(), e);
            return;
        } catch (InterruptedException e) {
            CJSystem.logging().error(getClass(), e);
            return;
        }
    }

    private IEndPortContainer createEndPortContainer() {
        return new EndPortContainer(site);
    }

    private void buildOkHttpClient() {
        RestFullConfig config = nodeConfig.getRestFull();
        restfull = new OkHttpClient().newBuilder()
                .readTimeout(config.readTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(config.connectTimeout(), TimeUnit.MILLISECONDS)
                .build();
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

    class TerminalServiceProvider implements IJPushServiceProvider {

        @Override
        public Object getService(String serviceId) {
            if ("$.terminal.server".equals(serviceId)) {
                return nodeServer;
            }
            if ("$.terminal.config".equals(serviceId)) {
                return nodeConfig;
            }
            if ("$.terminal.restfull".equals(serviceId)) {
                return restfull;
            }
            if ("$.terminal.endPortContainer".equals(serviceId)) {
                return endPortContainer;
            }
            if ("$.terminal.rabbitMQConsumer".equals(serviceId)) {
                return rabbitMQConsumer;
            }
            return null;
        }
    }
}

