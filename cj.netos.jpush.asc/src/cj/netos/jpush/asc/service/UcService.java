package cj.netos.jpush.asc.service;

import cj.netos.jpush.asc.IUcService;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.CheckAccessTokenException;
import cj.studio.openport.util.Encript;
import cj.ultimate.gson2.com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CjService(name = "ucService")
public class UcService implements IUcService, IServiceAfter {
    @CjServiceSite
    IServiceSite site;
    OkHttpClient client;

    @Override
    public void onAfter(IServiceSite site) {
        client = new OkHttpClient();
    }

    @Override
    public Map<String, Object> checkAccessToken(String accessToken) throws CircuitException {
       String appid = site.getProperty("appid");
        String  appKey = site.getProperty("appKey");
        String  appSecret = site.getProperty("appSecret");
        String  portsAuth = site.getProperty("ports.auth");
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
