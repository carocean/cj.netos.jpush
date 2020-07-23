package cj.netos.jpush.terminal.cmd;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IPipeline;
import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.IEndPortContainer;
import cj.netos.jpush.terminal.INodeConfig;
import cj.netos.jpush.terminal.ITerminalCommand;
import cj.netos.jpush.terminal.RestFullConfig;
import cj.netos.jpush.util.Encript;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.channel.Channel;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginTerminalCommand implements ITerminalCommand {

    @Override
    public String cmd() {
        return "login";
    }

    //注册到rabbitmq
    //生成终结点
    //添加到终结点容器
    //将终结点设置到pipeline上
    @Override
    public void exec(JPushFrame frame, IPipeline pipeline) throws CircuitException {
        String accessToken = frame.head("accessToken");
        INodeConfig config = (INodeConfig) pipeline.site().getService("$.terminal.config");
        OkHttpClient client = (OkHttpClient) pipeline.site().getService("$.terminal.restfull");
        Map<String, Object> info = null;
        try {
            info = verifyAccessToken(client, config.getRestFull(), accessToken);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
        IEndPortContainer endPortContainer = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        EndPort endPort = endPortContainer.online((Channel) pipeline.attachment(), info);
        pipeline.endPort(endPort);
    }

    private Map<String, Object> verifyAccessToken(OkHttpClient client, RestFullConfig config, String accessToken) throws IOException, CircuitException {
        String url = String.format("%s?token=%s", config.ports().get("uc.auth"), accessToken);
        String nonce = Encript.md5(String.format("%s%s", UUID.randomUUID().toString(), System.currentTimeMillis()));
        String sign = Encript.md5(String.format("%s%s%s", config.appKey(), nonce, config.appSecret()));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "verification")
                .addHeader("app-id", config.appid())
                .addHeader("app-key", config.appKey())
                .addHeader("app-nonce", nonce)
                .addHeader("app-sign", sign)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.code() >= 400) {
            throw new CircuitException(response.code() + "", response.message());
        }
        String body = response.body().string();
        Map<String, Object> map = new Gson().fromJson(body, HashMap.class);
        if (Double.valueOf(map.get("status") + "") >= 400) {
            throw new CircuitException(map.get("status") + "", map.get("message") + "");
        }
        String json = map.get("dataText") + "";
        map = new Gson().fromJson(json, HashMap.class);
        return map;
    }

}
