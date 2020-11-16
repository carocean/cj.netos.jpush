package cj.netos.jpush.terminal.plugin.buddy;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.plugin.IBuddyPusher;
import cj.netos.jpush.terminal.plugin.NotificationParser;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import com.vivo.push.sdk.notofication.Message;
import com.vivo.push.sdk.notofication.Result;
import com.vivo.push.sdk.server.Sender;

import java.util.UUID;

@CjService(name = "vivoBuddyPusher")
public class VivoBuddyPusher implements IBuddyPusher, IServiceAfter {

    private String secret;
    private int appid;
    private String appKey;

    @Override
    public void onAfter(IServiceSite site) {
        secret = site.getProperty("buddy.pusher.vivo.secretKey");
        String appid = site.getProperty("buddy.pusher.vivo.appid");
        this.appid = Integer.valueOf(appid);
        appKey = site.getProperty("buddy.pusher.vivo.appKey");
    }

    @Override
    public void push(JPushFrame frame, String regId) {
        Sender sender = null;//注册登录开发平台网站获取到的appSecret
        try {
            sender = new Sender(secret);
            Result result = sender.getToken(appid, appKey);//注册登录开发平台网站获取到的appId和appKey
            sender.setAuthToken(result.getAuthToken());
            Message singleMessage = new Message.Builder()
                    //该测试手机设备订阅推送所得的regid，且已添加为测试设备
                    .regId(regId)
                    .notifyType(4)
                    .title(NotificationParser.parseTitle(frame))
                    .content(NotificationParser.parseContent(frame))
                    .timeToLive(10000)
                    .skipType(2)
//                    .skipContent("http://www.vivo.com")
                    .networkType(-1)
                    .requestId(UUID.randomUUID().toString())
//推送模式 0：正式推送；1：测试推送，不填默认为0（测试推送，只能给web界面录入的测试用户推送；审核中应用，只能用测试推送）
                    .pushMode(1)
                    .build();
            Result resultMessage = sender.sendSingle(singleMessage);
            /*
            注意：出现10071错误是vivo限制半夜推通知
            { resultCode：10071
  description：超出发送时间允许范围
}
             */
            System.out.println(resultMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
