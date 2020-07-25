package cj.netos.jpush.terminal;

import cj.studio.ecm.CJSystem;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;

public class AscRegistry implements IAscRegistry {
    CuratorFramework framework;
    private INodeConfig nodeConfig;

    @Override
    public void start(INodeConfig nodeConfig) throws Exception {
        this.nodeConfig = nodeConfig;
        AscConfig ascConfig = nodeConfig.getAscConfig();
        ServerConfig serverConfig = nodeConfig.getServerConfig();

        String connectString = ascConfig.getConnectString();
        int retryIntervalMs = ascConfig.getRetryIntervalMs();
        int sessionTimeoutMs = ascConfig.getSessionTimeoutMs();
        int connectionTimeoutMs = ascConfig.getConnectionTimeoutMs();
        String namespace = ascConfig.getNamespace();
        RetryPolicy retryPolicy = new RetryForever(retryIntervalMs);
        framework =
                CuratorFrameworkFactory.builder()
                        .connectString(connectString)
                        .sessionTimeoutMs(sessionTimeoutMs)
                        .connectionTimeoutMs(connectionTimeoutMs)
                        .retryPolicy(retryPolicy)
                        .namespace(namespace)
                        .build();
        framework.start();
        CJSystem.logging().info(getClass(), String.format("Curator启动成功。connectString=%s, sessionTimeoutMs=%s, connectionTimeoutMs=%s, retryIntervalMs=%s, namespace=%s", connectString, sessionTimeoutMs, connectionTimeoutMs, retryIntervalMs, namespace));

        mountPath(ascConfig, serverConfig);
    }

    private void mountPath(AscConfig ascConfig, ServerConfig serverConfig) throws Exception {
        String mountFullPath = String.format("/asc%s", ascConfig.getMountPath());

        if (framework.checkExists().forPath(mountFullPath) != null) {
            framework.delete().guaranteed().forPath(mountFullPath);
        }
        framework.create().withMode(CreateMode.EPHEMERAL).forPath(mountFullPath, serverConfig.getOpenports().getBytes());
        CJSystem.logging().info(getClass(), String.format("终端已挂载，路径是:%s。", mountFullPath));

    }

    private void unmountPath(AscConfig ascConfig, ServerConfig serverConfig) throws Exception {
        String mountFullPath = String.format("/asc%s", ascConfig.getMountPath());

        if (framework.checkExists().forPath(mountFullPath) != null) {
            framework.delete().guaranteed().forPath(mountFullPath);
            CJSystem.logging().info(getClass(), String.format("终端卸载，路径是:%s。", mountFullPath));
        }
    }

    @Override
    public void stop() throws Exception {
        AscConfig ascConfig = nodeConfig.getAscConfig();
        ServerConfig serverConfig = nodeConfig.getServerConfig();
        unmountPath(ascConfig, serverConfig);
        framework.close();
    }
}
