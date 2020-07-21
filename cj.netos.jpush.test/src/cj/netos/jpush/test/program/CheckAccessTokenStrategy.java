package cj.netos.jpush.test.program;

import cj.studio.ecm.IServiceSite;
import cj.studio.openport.CheckAccessTokenException;
import cj.studio.openport.DefaultSecuritySession;
import cj.studio.openport.ICheckAccessTokenStrategy;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.util.Encript;
import cj.ultimate.gson2.com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckAccessTokenStrategy implements ICheckAccessTokenStrategy {
    String portsAuth;
    OkHttpClient client;
    String appKey;
    String appSecret;
    String appid;
    @Override
    public void init(IServiceSite site) {
        appid = site.getProperty("appid");
        appKey = site.getProperty("appKey");
        appSecret = site.getProperty("appSecret");
        portsAuth = site.getProperty("ports.auth");
        client = new OkHttpClient();
    }

    @Override
    public ISecuritySession checkAccessToken(ISecuritySession securitySession, String portsurl, String methodName, String accessToken) throws CheckAccessTokenException {
        Map<String, Object> tokeninfo = _checkAccessToken(accessToken);
        List<String> roles = (List<String>) tokeninfo.get("roles");
        ISecuritySession _securitySession = new DefaultSecuritySession(tokeninfo.get("person") + "", roles, null);
        int pos=_securitySession.principal().lastIndexOf("@");
        String appid = _securitySession.principal().substring(pos + 1);
        _securitySession.property("appid", appid);
        return _securitySession;
    }

    private Map<String, Object> _checkAccessToken(String accessToken) throws CheckAccessTokenException {
        String nonce = Encript.md5(String.format("%s%s", UUID.randomUUID().toString(), System.currentTimeMillis()));
        String sign = Encript.md5(String.format("%s%s%s", appKey, nonce, appSecret));
        String url = String.format("%s?token=%s", portsAuth, accessToken);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "verification")
                .addHeader("app-id", appid)
                .addHeader("app-key", appKey)
                .addHeader("app-nonce", nonce)
                .addHeader("app-sign", sign)
                .get()
                .build();
        final Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new CheckAccessTokenException("1002", e);
        }
        if (response.code() >= 400) {
            throw new CheckAccessTokenException("1002", String.format("远程访问失败:%s", response.message()));
        }
        String json = null;
        try {
            json = response.body().string();
        } catch (IOException e) {
            throw new CheckAccessTokenException("1002", e);
        }
        Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
        if (Double.parseDouble(map.get("status") + "") >= 400) {
            throw new CheckAccessTokenException(map.get("status") + "", map.get("message") + "");
        }
        json = (String) map.get("dataText");
        map = new Gson().fromJson(json, HashMap.class);
        return map;
    }

}
