package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;

@CjService(name = "buddyPusherFactory")
public class BuddyPusherFactory implements IBuddyPusherFactory {
    @CjServiceRef
    IBuddyPusher vivoBuddyPusher;
    @CjServiceRef
    IBuddyPusher oppoBuddyPusher;
    @CjServiceRef
    IBuddyPusher xiaomiBuddyPusher;
    @CjServiceRef
    IBuddyPusher huaweiBuddyPusher;

    @Override
    public void push(JPushFrame frame, String device) {
        int pos = device.indexOf("://");
        String brand = device.substring(0, pos);
        String regId = device.substring(pos + 3, device.length());
        switch (brand) {
            case "huawei":
                huaweiBuddyPusher.push(frame, regId);
                break;
            case "vivo":
                vivoBuddyPusher.push(frame, regId);
                break;
            case "oppo":
                oppoBuddyPusher.push(frame, regId);
                break;
            case "xiaomi":
                xiaomiBuddyPusher.push(frame, regId);
                break;
            default:
                CJSystem.logging().error(getClass(), String.format("不支持的合作推送厂商:%s", brand));
                break;
        }
    }

}
