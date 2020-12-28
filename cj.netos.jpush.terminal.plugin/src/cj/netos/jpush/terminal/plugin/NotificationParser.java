package cj.netos.jpush.terminal.plugin;

import cj.netos.jpush.JPushFrame;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class NotificationParser {

    public static String parseContent(JPushFrame frame) {
        String content = "";
        if (frame.url().startsWith("/chat/room/message")) {
            byte[] data = frame.content().readFully();
            String text = new String(data);
            String contentType = frame.parameter("contentType");
            switch (frame.command()) {
                case "pushMessage":
                    switch (contentType) {
                        case "text":
                            if (StringUtil.isEmpty(text)) {
                                content = "发信息给你！";
                            } else {
                                content = text.length() > 40 ? text.substring(0, 40) : text;
                            }
                            break;
                        case "audio":
                            content = "发送语音给您！";
                            break;
                        case "image":
                            content = "发送图片给您！";
                            break;
                        case "video":
                            content = "发送视频给您！";
                            break;
                        case "transTo":
                            content = "转账给您！";
                            break;
                        case "absorbTo":
                            String amount = frame.parameter("amount");
                            String times = frame.parameter("times");
                            if (StringUtil.isEmpty(amount)) {
                                break;
                            }
                            BigDecimal bigAmount=new BigDecimal(amount);
                            if (bigAmount.compareTo(BigDecimal.ZERO) == 0) {
                                break;
                            }
                            bigAmount=bigAmount.divide(new BigDecimal("100.00"),14, RoundingMode.DOWN);
                            content = String.format("¥%s 本次累计%s笔", bigAmount, times);
                            break;
                        default:
                            content = "有新消息";
                            break;
                    }
                    break;
                default:
                    content = "有新消息";
                    break;
            }
        } else if (frame.url().startsWith("/netflow/channel")) {
            byte[] data = frame.content().readFully();
            String text = new String(data);
//            CJSystem.logging().info(NotificationParser.class.getClass(),String.format("netflow:%s",text));
            switch (frame.command()) {
                case "pushDocument":
                    if (StringUtil.isEmpty(text)) {
                        content = "发信息给你！";
                    } else {
                        Map<String, Object> map = new Gson().fromJson(text, HashMap.class);
                        content = (String) map.get("content");
                        if (StringUtil.isEmpty(content)) {
                            content = "发信息给你！";
                        }
                        content = content.length() > 40 ? content.substring(0, 40) : content;
                    }
                    break;
                case "likeDocument":
                    content = "给您点赞！";
                    break;
                case "unlikeDocument":
                    content = "取消点赞！";
                    break;
                case "commentDocument":
                    content = "评论了您！";
                    break;
                case "uncommentDocument":
                    content = "删除了给您的评论！";
                    break;
                case "mediaDocument":
                    content = "发图给您！";
                    break;
                default:
                    content = "信息给你！";
                    break;
            }
        } else if (frame.url().startsWith("/geosphere/receptor")) {
            byte[] data = frame.content().readFully();
            String text = new String(data);
//            CJSystem.logging().info(NotificationParser.class.getClass(),String.format("geosphere:%s",text));
            switch (frame.command()) {
                case "pushDocument":
                    if (StringUtil.isEmpty(text)) {
                        content = "发信息给你！";
                    } else {
                        Map<String, Object> map = new Gson().fromJson(text, HashMap.class);
                        content = (String) map.get("text");
                        if (StringUtil.isEmpty(content)) {
                            content = "发信息给你！";
                        }
                        content = content.length() > 40 ? content.substring(0, 40) : content;
                    }
                    break;
                case "likeDocument":
                    content = "给您点赞！";
                    break;
                case "unlikeDocument":
                    content = "取消点赞！";
                    break;
                case "commentDocument":
                    content = "评论了您！";
                    break;
                case "uncommentDocument":
                    content = "删除了给您的评论！";
                    break;
                case "mediaDocument":
                    content = "发图给您！";
                    break;
                default:
                    content = "信息给你！";
                    break;
            }
        } else {
            content = "信息给你！";
        }
        return content;
    }

    public static String parseTitle(JPushFrame frame) {
//        CJSystem.logging().info(NotificationParser.class.getClass(), new String(frame.copy().toBytes()));
        String title = "";
        String contentType = frame.parameter("contentType");
        String sender = frame.head("sender-nick");
        if (frame.url().startsWith("/chat/room/message")) {

            switch (frame.command()) {
                case "pushMessage":
                    switch (contentType) {
                        case "text":
                            title = String.format("%s 对你说：", sender);
                            break;
                        case "audio":
                        case "image":
                        case "video":
                            title = String.format("%s：", sender);
                            break;
                        case "transTo":
                            title = String.format("%s：", sender);
                            break;
                        case "absorbTo":
                            title = String.format("洇金到：");
                            break;
                        default:
                            title = "发信息给你";
                            break;
                    }
                    break;
                default:
                    title = "发信息给你";
                    break;
            }
        } else if (frame.url().startsWith("/netflow/channel")) {
            switch (frame.command()) {
                case "pushDocument":
                    title = String.format("%s 发来网流：", sender);
                    break;
                case "likeDocument":
                    title = String.format("%s：", sender);
                    break;
                case "unlikeDocument":
                    title = String.format("%s：", sender);
                    break;
                case "commentDocument":
                    title = String.format("%s：", sender);
                    break;
                case "uncommentDocument":
                    title = String.format("%s：", sender);
                    break;
                case "mediaDocument":
                    title = String.format("%s：", sender);
                    break;
                default:
                    title = String.format("%s：", sender);
                    break;
            }
        } else if (frame.url().startsWith("/geosphere/receptor")) {
            switch (frame.command()) {
                case "pushDocument":
                    title = String.format("%s 发来地圈：", sender);
                    break;
                case "likeDocument":
                    title = String.format("%s：", sender);
                    break;
                case "unlikeDocument":
                    title = String.format("%s：", sender);
                    break;
                case "commentDocument":
                    title = String.format("%s：", sender);
                    break;
                case "uncommentDocument":
                    title = String.format("%s：", sender);
                    break;
                case "mediaDocument":
                    title = String.format("%s：", sender);
                    break;
                default:
                    title = String.format("%s：", sender);
                    break;
            }
        } else {
            title = String.format("%s：", sender);
        }
        return title;
    }

}
