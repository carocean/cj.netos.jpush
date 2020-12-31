package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.util.Encript;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSetter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@CjService(name = "absorbNotifyWriter")
public class AbsorbNotifyWriter implements IAbsorbNotifyWriter, IServiceAfter, IServiceSetter {
    static final String KEY_RECEIVER = "jpusher.cache.receivers";
    static final String KEY_RECEIVER_PERSON = "jpusher.cache.persons";
    @CjServiceRef(refByName = "@.redis.cluster")
    JedisCluster jedisCluster;
    IBuddyPusherFactory buddyPusherFactory;
    String portsPerson;
    OkHttpClient client;
    String appKey;
    String appSecret;
    String appid;
    Timer timer;

    @Override
    public void onAfter(IServiceSite site) {
        appid = site.getProperty("appid");
        appKey = site.getProperty("appKey");
        appSecret = site.getProperty("appSecret");
        portsPerson = site.getProperty("ports.person");
        client = new OkHttpClient();
        timer = new Timer();
        CJSystem.logging().info(getClass(), String.format("洇金推送检测程序启动，检测间隔，每10分钟"));
        timer.schedule(new DefaultTimerTask(), 4000L, 10 * 60 * 1000L);//每10分钟检测一次
    }

    @Override
    public void setService(String serviceId, Object service) {
        buddyPusherFactory = (IBuddyPusherFactory) service;
    }

    @Override
    public void addAmount(String receiverPerson, String receiverNick, String device, BigDecimal amount) {
        String key = String.format("%s/%s", KEY_RECEIVER, receiverPerson);
        Map<String, String> exists = jedisCluster.hgetAll(key);
        if (exists == null) {
            return;
        }
        String vAmount = exists.get("amount");
        BigDecimal valueAmount = BigDecimal.ZERO;
        if (!StringUtil.isEmpty(vAmount)) {
            valueAmount = new BigDecimal(vAmount);
        }
        valueAmount = valueAmount.add(amount);
        Map<String, String> map = new HashMap<>();
        map.put("amount", valueAmount + "");

        String vtimes = exists.get("times");
        int valuetimes = 0;
        if (!StringUtil.isEmpty(vtimes)) {
            valuetimes = Integer.valueOf(vtimes);
        }
        valuetimes+=1;
        map.put("times", valuetimes + "");

        map.put("device", device);
        jedisCluster.hset(key, map);
        jedisCluster.hset(KEY_RECEIVER_PERSON, receiverPerson, receiverNick);
//        CJSystem.logging().info(getClass(), String.format("添加洇金，%s=%s", key, value));
    }

    @Override
    public String getSenderNick(String person) throws CircuitException {
        String key = "jpusher.cache.senders";
        String pkey = String.format("%s/%s", key, person);
        if (jedisCluster.exists(pkey)) {
            return jedisCluster.get(pkey);
        }
        String nick = getPersonNickOnUC(person);
        if (StringUtil.isEmpty(nick)) {
            return null;
        }
        jedisCluster.set(pkey, nick);
        return nick;
    }

    private String getPersonNickOnUC(String person) throws CircuitException {
        String nonce = Encript.md5(String.format("%s%s", UUID.randomUUID().toString(), System.currentTimeMillis()));
        String sign = Encript.md5(String.format("%s%s%s", appKey, nonce, appSecret));
        String url = String.format("%s?person=%s", portsPerson, person);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "findPersonOnSecurity")
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
            throw new CircuitException("1002", e);
        }
        if (response.code() >= 400) {
            throw new CircuitException("1002", String.format("远程访问失败:%s", response.message()));
        }
        String json = null;
        try {
            json = response.body().string();
        } catch (IOException e) {
            throw new CircuitException("1002", e);
        }
        Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
        if (Double.parseDouble(map.get("status") + "") >= 400) {
            throw new CircuitException(map.get("status") + "", map.get("message") + "");
        }
        json = (String) map.get("dataText");
        map = new Gson().fromJson(json, HashMap.class);
        if (map == null) {
            return null;
        }
        return (String) map.get("nickName");
    }

    @Override
    public void checkAndSendAbsorbNotify(String person, String nick) throws CircuitException {
        String key = String.format("%s/%s", KEY_RECEIVER, person);
        Map<String, String> map = jedisCluster.hgetAll(key);
        if (map == null||map.isEmpty()) {
            return;
        }
        String device = map.get("device");
        String amount = map.get("amount");
        String times = map.get("times");
//                CJSystem.logging().info(getClass(), String.format("%s %s=%s", nick, key, map));
        JPushFrame frame = new JPushFrame("pushMessage /chat/room/message netos/1.0");
        frame.head("to-person", person);
        frame.head("to-nick", nick);
        frame.parameter("amount", amount);
        frame.parameter("times", times);
        frame.parameter("contentType", "absorbTo");
        buddyPusherFactory.push(frame, device);

        jedisCluster.del(key);
        jedisCluster.hdel(KEY_RECEIVER_PERSON, person);
    }

    class DefaultTimerTask extends TimerTask {

        @Override
        public void run() {
            Map<String, String> persons = jedisCluster.hgetAll(KEY_RECEIVER_PERSON);
//            CJSystem.logging().info(getClass(), String.format("%s/%s", KEY_RECEIVER, persons));
            for (String person : persons.keySet()) {
                String nick = persons.get(person);
                try {
                    checkAndSendAbsorbNotify(person, nick);
                } catch (Exception e) {
                    CJSystem.logging().info(getClass(), e.getMessage());
                }
            }

        }
    }
}
