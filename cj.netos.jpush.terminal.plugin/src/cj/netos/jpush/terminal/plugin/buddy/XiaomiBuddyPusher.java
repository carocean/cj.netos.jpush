package cj.netos.jpush.terminal.plugin.buddy;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.plugin.IBuddyPusher;
import cj.netos.jpush.terminal.plugin.NotificationParser;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import com.oppo.push.server.Notification;
import com.oppo.push.server.Target;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;
import org.json.simple.parser.ParseException;

import java.io.IOException;

@CjService(name = "xiaomiBuddyPusher")
public class XiaomiBuddyPusher implements IBuddyPusher, IServiceAfter {

    private String secret;

    @Override
    public void onAfter(IServiceSite site) {
        secret = site.getProperty("buddy.pusher.xiaomi.secretKey");
    }

    @Override
    public void push(JPushFrame frame, String regId) {
        Constants.useOfficial();
        com.xiaomi.xmpush.server.Sender sender = new Sender(secret);
        String messagePayload = NotificationParser.parseContent(frame);
        String title = NotificationParser.parseTitle(frame);
        String description =messagePayload;
        Message message = new Message.Builder()
                .title(title)
                .description(description)
                .restrictedPackageName("cj.netos.netos_app")
                .notifyType(-1)     //-1（系统默认值）：以上三种效果都有
                .passThrough(0)  //消息使用通知栏方式
                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY)
                .build();
        Result result = null;
        try {
            result = sender.send(message, regId, 3);////根据regID，发送消息到指定设备上
            CJSystem.logging().info(getClass(),String.format("%s",result));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
