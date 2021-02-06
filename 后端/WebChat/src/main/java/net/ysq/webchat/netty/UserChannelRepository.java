package net.ysq.webchat.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.ysq.webchat.netty.entity.TextMsgModel;
import net.ysq.webchat.utils.JsonUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户id和channel关联的仓库
 *
 * @author passerbyYSQ
 * @create 2021-02-05 23:20
 */
public class UserChannelRepository {

    private static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, Channel> USER_CHANNEL = new ConcurrentHashMap<>();

    public static void bind(String userId, Channel channel) {
        // 此时channel一定已经在ChannelGroup中了

        // 双向绑定
        // channel -> userId
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        channel.attr(key).set(userId);
        // userId -> channel
        USER_CHANNEL.put(userId, channel);
    }

    public static Channel get(String userId) {
        return USER_CHANNEL.get(userId);
    }

    public static void add(Channel channel) {
        CHANNEL_GROUP.add(channel);
    }

    public static void remove(Channel channel) {
        // 移除映射关系： userId -> channel
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = channel.attr(key).get();
        System.out.println("-----");
        System.out.println(userId);
        System.out.println(USER_CHANNEL);
        // userId有可能为空。可能chanelActive之后，由于前端原因（或者网络原因）没有及时绑定userId。
        // 此时netty认为channelInactive了，就移除通道，就会调用此处。这里的userId就是null
        if (!StringUtils.isEmpty(userId)) {
            USER_CHANNEL.remove(userId);
        }

        CHANNEL_GROUP.remove(channel);
    }

    /**
     * 判断用户是否在线
     * map和channelGroup中均能找得到对应的channel说明用户在线
     * @return      在线就返回对应的channel，不在线返回null
     */
    public static Channel isOnline(String userId) {
        Channel channel = USER_CHANNEL.get(userId);
        if (ObjectUtils.isEmpty(channel)) {
            return null;
        }
        return CHANNEL_GROUP.find(channel.id());
    }

    public static void pushMsg(String receiverId, TextMsgModel msgModel) {
        Channel receiverChannel = isOnline(receiverId);
        if (!ObjectUtils.isEmpty(receiverChannel)) {
            // 在线，就推送；离线，不做处理
            String json = JsonUtils.objectToJson(msgModel);
            TextWebSocketFrame frame = new TextWebSocketFrame(json);
            receiverChannel.writeAndFlush(frame);
            System.out.println("消息推送：" + msgModel.getMsg().getContent());
        } else {
            // 离线状态
            System.out.println(receiverId + "用户离线");
        }
    }


    public static void print() {
        System.out.println("所有通道的长id：");
        for (Channel channel : CHANNEL_GROUP) {
            System.out.println(channel.id().asLongText());
        }
        System.out.println("userId -> channel 的映射：");
        for (Map.Entry<String, Channel> entry : USER_CHANNEL.entrySet()) {
            System.out.println("userId:" + entry.getKey() + "--->"
                    + "channelId:" + entry.getValue().id().asLongText());
        }
    }

}
