package cj.netos.jpush.asc;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenports;

@CjOpenports(usage = "推送终端注册中心")
public interface IAscPorts extends IOpenportService {

    @CjOpenport(usage = "获取可用的终端地址列表")
    String[] getTerminalAddressList(ISecuritySession securitySession) throws CircuitException;
}
