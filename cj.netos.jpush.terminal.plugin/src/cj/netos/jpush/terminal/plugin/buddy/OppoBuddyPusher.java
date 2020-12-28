package cj.netos.jpush.terminal.plugin.buddy;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.plugin.IBuddyPusher;
import cj.netos.jpush.terminal.plugin.NotificationParser;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import com.oppo.push.server.*;

@CjService(name = "oppoBuddyPusher")
public class OppoBuddyPusher implements IBuddyPusher, IServiceAfter {

    private String secret;
    private String appKey;

    @Override
    public void onAfter(IServiceSite site) {
        secret = site.getProperty("buddy.pusher.oppo.secretKey");
        appKey = site.getProperty("buddy.pusher.oppo.appKey");
    }

    @Override
    public void push(JPushFrame frame, String regId) {
        //●  创建sender对象
//使用appKey, masterSecret创建sender对象（每次发送消息都使用这个sender对象）
        com.oppo.push.server.Sender sender = null;
        try {
            sender = new Sender(appKey, secret);
//●  发送单推通知栏消息
            Notification notification = new Notification(); //创建通知栏消息体
            notification.setTitle(NotificationParser.parseTitle(frame));
            notification.setContent(NotificationParser.parseContent(frame));
            Target target = Target.build(regId); //创建发送对象
            target.setTargetType(TargetType.REGISTRATION_ID);
            Result result = sender.unicastNotification(notification, target);  //发送单推消息
            CJSystem.logging().info(getClass(),String.format("%s",result));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
