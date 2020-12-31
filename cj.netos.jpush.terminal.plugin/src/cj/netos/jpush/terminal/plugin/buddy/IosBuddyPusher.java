package cj.netos.jpush.terminal.plugin.buddy;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.terminal.plugin.IBuddyPusher;
import cj.netos.jpush.terminal.plugin.NotificationParser;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import com.notnoop.apns.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@CjService(name = "iosBuddyPusher")
public class IosBuddyPusher implements IBuddyPusher, IServiceAfter {

    ApnsService svc;
    boolean isProductEnv;
    @Override
    public void onAfter(IServiceSite site) {
        String cert = site.getProperty("buddy.pusher.ios.cert");
        String pwd = site.getProperty("buddy.pusher.ios.pwd");
        String env = site.getProperty("buddy.pusher.ios.env");
        isProductEnv="product".equals(env);
        String usr = site.getProperty("home.dir");
        cert = String.format("%s%s", usr, cert);
        CJSystem.logging().info(getClass(), "cert path is " + cert);
        final ApnsDelegate delegate = new ApnsDelegate() {
            public void messageSent(final ApnsNotification message, final boolean resent) {
                CJSystem.logging().info(getClass(), "Sent message " + message + " Resent: " + resent);
            }

            public void messageSendFailed(final ApnsNotification message, final Throwable e) {
                CJSystem.logging().info(getClass(), "Failed message " + message);

            }

            public void connectionClosed(final DeliveryError e, final int messageIdentifier) {
                CJSystem.logging().info(getClass(), "Closed connection: " + messageIdentifier + "\n   deliveryError " + e.toString());
            }

            public void cacheLengthExceeded(final int newCacheLength) {
                CJSystem.logging().info(getClass(), "cacheLengthExceeded " + newCacheLength);

            }

            public void notificationsResent(final int resendCount) {
                CJSystem.logging().info(getClass(), "notificationResent " + resendCount);
            }
        };
        try {
            svc = APNS.newService()
                    .withAppleDestination(isProductEnv)//true为生产环境，false是测试环境
                    .withCert(new FileInputStream(cert), pwd)
                    .withDelegate(delegate)
                    .build();
            svc.start();
        } catch (FileNotFoundException e) {
            CJSystem.logging().error(getClass(), e);
            throw new EcmException(e);
        }
    }

    @Override
    public void push(JPushFrame frame, String regId) throws CircuitException {
        String title = NotificationParser.parseTitle(frame);
        String body = NotificationParser.parseContent(frame);
        String unreadCount = frame.head("unread-count");
        if (StringUtil.isEmpty(unreadCount)) {
            unreadCount = "1";
        }
        final String payload = APNS.newPayload().sound("default").badge(Long.valueOf(unreadCount).intValue()).alertTitle(title).alertBody(body).build();
        final ApnsNotification goodMsg = svc.push(regId, payload);
        CJSystem.logging().info(getClass(), String.format("%s", goodMsg));
//        final Map<String, Date> inactiveDevices = svc.getInactiveDevices();
//        for (final Map.Entry<String, Date> ent : inactiveDevices.entrySet()) {
//            System.out.println("Inactive " + ent.getKey() + " at date " + ent.getValue());
//        }
    }

}
