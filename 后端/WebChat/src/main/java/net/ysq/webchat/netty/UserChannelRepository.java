package net.ysq.webchat.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.ysq.webchat.netty.entity.MsgModel;
import net.ysq.webchat.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger logger = LoggerFactory.getLogger(UserChannelRepository.class);

    public static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, Channel> USER_CHANNEL = new ConcurrentHashMap<>();

    public static synchronized void bind(String userId, Channel channel) {
        // 此时channel一定已经在ChannelGroup中了

        // 之前已经绑定过了，移除并释放掉之前绑定的channel
        if (USER_CHANNEL.containsKey(userId)) { // map  userId --> channel
            Channel oldChannel = USER_CHANNEL.get(userId);
            CHANNEL_GROUP.remove(oldChannel);
            oldChannel.close();
        }

        // 双向绑定
        // channel -> userId
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        channel.attr(key).set(userId);

        // userId -> channel
        USER_CHANNEL.put(userId, channel);
    }

    /**
     * 从通道中获取userId。只要userId和channel绑定周，这个方法就一定能获取的到
     * @param channel
     * @return
     */
    public static String getUserId(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        return channel.attr(key).get();
    }

    public static void add(Channel channel) {
        CHANNEL_GROUP.add(channel);
    }

    public static synchronized void remove(Channel channel) {
        String userId = getUserId(channel);

        // userId有可能为空。可能chanelActive之后，由于前端原因（或者网络原因）没有及时绑定userId。
        // 此时netty认为channelInactive了，就移除通道，这时userId就是null
        if (!StringUtils.isEmpty(userId)) {
            USER_CHANNEL.remove(userId); // map
        }

        CHANNEL_GROUP.remove(channel);

        // 关闭channel
        channel.close();
    }

    /**
     * 判断用户是否在线
     * map和channelGroup中均能找得到对应的channel说明用户在线
     * @return      在线就返回对应的channel，不在线返回null
     */
    public static Channel isOnline(String userId) {
        Channel channel = USER_CHANNEL.get(userId); // map
        if (ObjectUtils.isEmpty(channel)) {
            return null;
        }
        return CHANNEL_GROUP.find(channel.id());
    }

    public static void pushMsg(String receiverId, MsgModel msgModel) {
        Channel receiverChannel = isOnline(receiverId);
        if (!ObjectUtils.isEmpty(receiverChannel)) {
            // 在线，就推送；离线，不做处理
            ObjectMapper mapper = SpringUtils.getBean(ObjectMapper.class);
            String json = null;
            try {
                json = mapper.writeValueAsString(msgModel);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            TextWebSocketFrame frame = new TextWebSocketFrame(json);
            receiverChannel.writeAndFlush(frame);
        } else {
            // 离线状态
            logger.info("{} 用户离线", receiverId);
        }
    }


    public synchronized static void print() {
        logger.info("所有通道的长id：");
        for (Channel channel : CHANNEL_GROUP) {
            logger.info(channel.id().asLongText());
        }
        logger.info("userId -> channel 的映射：");
        for (Map.Entry<String, Channel> entry : USER_CHANNEL.entrySet()) {
            logger.info("userId: {} ---> channelId: {}", entry.getKey(), entry.getValue().id().asLongText());
        }
    }

}
