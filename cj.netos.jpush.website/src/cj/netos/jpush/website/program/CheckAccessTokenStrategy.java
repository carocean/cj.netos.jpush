package cj.netos.jpush.website.program;

import cj.netos.jpush.website.IUcService;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.CheckAccessTokenException;
import cj.studio.openport.DefaultSecuritySession;
import cj.studio.openport.ICheckAccessTokenStrategy;
import cj.studio.openport.ISecuritySession;

import java.util.List;
import java.util.Map;

public class CheckAccessTokenStrategy implements ICheckAccessTokenStrategy {
    IUcService ucService;

    @Override
    public void init(IServiceSite site) {
        ucService = (IUcService) site.getService("ucService");
    }

    @Override
    public ISecuritySession checkAccessToken(ISecuritySession securitySession, String portsurl, String methodName, String accessToken) throws CheckAccessTokenException {
        Map<String, Object> tokeninfo = null;
        try {
            tokeninfo = ucService.checkAccessToken(accessToken);
        } catch (CircuitException e) {
            throw new CheckAccessTokenException(e.getStatus(), e.getMessage());
        }
        List<String> roles = (List<String>) tokeninfo.get("roles");
        ISecuritySession _securitySession = new DefaultSecuritySession(tokeninfo.get("person") + "", roles, null);
        int pos = _securitySession.principal().lastIndexOf("@");
        String appid = _securitySession.principal().substring(pos + 1);
        _securitySession.property("appid", appid);
        _securitySession.property("device", tokeninfo.get("device"));
        return _securitySession;
    }

}
