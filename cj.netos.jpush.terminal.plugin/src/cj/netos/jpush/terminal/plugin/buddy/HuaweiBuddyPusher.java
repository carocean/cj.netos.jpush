package cj.netos.jpush.terminal.plugin.buddy;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.plugin.IBuddyPusher;
import cj.netos.jpush.terminal.plugin.NotificationParser;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.ultimate.gson2.com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@CjService(name = "huaweiBuddyPusher")
public class HuaweiBuddyPusher implements IBuddyPusher, IServiceAfter {

    private String secret;
    private String appid;

    @Override
    public void onAfter(IServiceSite site) {
        secret = site.getProperty("buddy.pusher.huawei.secretKey");
        appid = site.getProperty("buddy.pusher.huawei.appid");
    }

    @Override
    public void push(JPushFrame frame, String regId) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        String requestBody = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s", appid, secret);
        Request request = new Request.Builder()
                .url("https://oauth-login.cloud.huawei.com/oauth2/v3/token")
                .post(RequestBody.create(mediaType, requestBody))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        //通过call去处理给你响应Response
        Response response = null;
        try {
            response = call.execute();
            //{"access_token":"CgB6e3x9CDWm7Ltbl4WKO7NHov99ZdPID47ksABADphaTTzgZyCcWVMiKQm7sjGcOOHndoPIR5HS7VX99d4mFezs","expires_in":3600,"token_type":"Bearer"}
            final String tokenJson = response.body().string();
            System.out.println(tokenJson);
            Map<String, String> map = new Gson().fromJson(tokenJson, HashMap.class);
            doPush(frame,okHttpClient, map.get("access_token"),regId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doPush(JPushFrame frame, OkHttpClient okHttpClient, String token, String regId) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        /***
         * 说明:
         * token是代表地微在华为手机设备由华为api给的token
         * 角标add_num和set_num同时用的话set_num有效。一个是增量一个是全量
         */
        String requestBody = "{\n" +
                "    \"validate_only\": false,\n" +
                "    \"message\": {\n" +
                "        \"notification\": {\n" +
                "            \"title\": \"%s \",\n" +
                "            \"body\": \"%s\"\n" +
                "        },\n" +
                "        \"android\": {\n" +
                "            \"notification\": {\n" +
                "                \"click_action\": {\n" +
                "                    \"type\": 3,\n" +
                "                    \"url\": \"\"\n" +
                "                },\n" +
                "                \"badge\": {\n" +
                "                    \"add_num\": 1,\n" +
                "                    \"class\": \"cj.netos.netos_app.MainActivity\"\n" +
//                "                    \"set_num\": 1\n" +
                "                }\n" +
                "            },\n" +
                "            \"ttl\": \"1000\"\n" +
                "        },\n" +
                "        \"token\": [\n" +
                "            \"%s\"\n" +
                "        ]\n" +
                "    }\n" +
                "}\n";
        requestBody=String.format(requestBody, NotificationParser.parseTitle(frame),NotificationParser.parseContent(frame),regId);
        Request request = new Request.Builder()
                .url("https://push-api.cloud.huawei.com/v1/103180143/messages:send")
                .addHeader("Authorization", String.format("Bearer %s", token))
                .post(RequestBody.create(mediaType, requestBody))
                .build();
        Call call = okHttpClient.newCall(request);
        //通过call去处理给你响应Response
        Response response = null;
        try {
            response = call.execute();
            //{"access_token":"CgB6e3x9CDWm7Ltbl4WKO7NHov99ZdPID47ksABADphaTTzgZyCcWVMiKQm7sjGcOOHndoPIR5HS7VX99d4mFezs","expires_in":3600,"token_type":"Bearer"}
            final String text = response.body().string();
            CJSystem.logging().info(getClass(),String.format("%s",text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
