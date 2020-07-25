package cj.netos.jpush.asc.service;

import cj.netos.jpush.asc.ITerminalNodeContainer;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CjService(name = "terminalNodeContainer")
public class TerminalNodeContainer implements ITerminalNodeContainer, TreeCacheListener {
    final static String ROOT_PATH = "/asc";
    @CjServiceRef(refByName = "curator.framework")
    CuratorFramework framework;
    List<String> terminalAddressList;

    @Override
    public String[] getTerminalAddressList() {
        return terminalAddressList.toArray(new String[0]);
    }

    @Override
    public void refresh() throws Exception {
        Stat stat = framework.checkExists().forPath(ROOT_PATH);
        if (stat == null) {
            framework.create().creatingParentsIfNeeded().forPath(ROOT_PATH);
        }
        CJSystem.logging().info(getClass(), String.format("注册中心根节点是：%s", ROOT_PATH));
        terminalAddressList = new CopyOnWriteArrayList<>();
        List<String> childs = framework.getChildren().forPath(ROOT_PATH);
        for (String child : childs) {
            String childPath = String.format("%s/%s", ROOT_PATH, child);
            byte[] v = framework.getData().forPath(childPath);
            String address = new String(v);
            terminalAddressList.add(address);
            CJSystem.logging().info(getClass(), String.format("发现终端：%s = %s", childPath, address));
        }
        TreeCache treeCache = new TreeCache(framework, ROOT_PATH);
        treeCache.getListenable().addListener(this);
        treeCache.start();
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        ChildData data = event.getData();
        if (data != null && data.getPath().equals(ROOT_PATH)) {
            return;
        }
        String address = null;
        switch (event.getType()) {
            case NODE_ADDED:
                address = new String(data.getData());
                for (int i = 0; i < terminalAddressList.size(); i++) {
                    String item = terminalAddressList.get(i);
                    if (item.equals(address)) {
                        terminalAddressList.remove(i);
                    }
                }
                terminalAddressList.add(address);
                CJSystem.logging().info(getClass(), String.format("发现新终端：%s = %s", data.getPath(), address));
                break;
            case NODE_UPDATED:
                address = new String(data.getData());
                for (int i = 0; i < terminalAddressList.size(); i++) {
                    String item = terminalAddressList.get(i);
                    if (item.equals(address)) {
                        terminalAddressList.remove(i);
                    }
                }
                terminalAddressList.add(address);
                CJSystem.logging().info(getClass(), String.format("更新终端：%s = %s", data.getPath(), address));
                break;
            case NODE_REMOVED:
                address = new String(data.getData());
                for (int i = 0; i < terminalAddressList.size(); i++) {
                    String item = terminalAddressList.get(i);
                    if (item.equals(address)) {
                        terminalAddressList.remove(i);
                        CJSystem.logging().info(getClass(), String.format("删除终端：%s = %s", data.getPath(), address));
                    }
                }
                break;
            default:
                break;
        }
    }
}
