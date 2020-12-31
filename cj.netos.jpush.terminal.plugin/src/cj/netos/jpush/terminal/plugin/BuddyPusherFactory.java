package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceInvertInjection;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    @CjServiceRef
    IBuddyPusher iosBuddyPusher;
    @CjServiceInvertInjection
    @CjServiceRef
    IAbsorbNotifyWriter absorbNotifyWriter;

    @Override
    public void push(JPushFrame frame, String device) throws CircuitException {
        //先把洇金通知过滤掉，因为太频繁了，今后改洇金通知方案为redis缓冲汇总方式，10分种检测，如果发现接收人有洇金则通知一次
        String receiverNick = frame.head("to-nick");
        String receiverPerson = frame.head("to-person");
        if ("/pay/absorbs".equals(frame.parameter("contentType"))) {//此处只写入洇金不通知
            byte[] data = frame.content().readFully();
            String text = new String(data);
            Map<String, Object> map = new Gson().fromJson(text, HashMap.class);
            BigDecimal amount = new BigDecimal(map.get("realAmount") + "");
            absorbNotifyWriter.addAmount(receiverPerson, receiverNick, device, amount);
            return;
        }else{
            //让非洇金事件携带洇金通知
            String contentType = frame.parameter("contentType");
            if (!"absorbTo".equals(contentType)) {
                try {
                    absorbNotifyWriter.checkAndSendAbsorbNotify(receiverPerson, receiverNick);
                } catch (Exception e) {
                    CJSystem.logging().error(getClass(), e);
                }
            }
        }

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
            case "ios":
                iosBuddyPusher.push(frame, regId);
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
