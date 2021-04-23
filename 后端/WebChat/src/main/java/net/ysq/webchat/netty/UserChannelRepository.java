package net.ysq.webchat.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import net.ysq.webchat.netty.entity.MsgActionEnum;
import net.ysq.webchat.netty.entity.MsgModel;
import net.ysq.webchat.utils.SpringUtils;
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
@Slf4j
public class UserChannelRepository {

    //private final static Logger logger = LoggerFactory.getLogger(UserChannelRepository.class);

    private static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, Channel> USER_CHANNEL = new ConcurrentHashMap<>();
    private static final Object bindLocker = new Object();
    private static final Object removeLocker = new Object();

    public static synchronized void bind(String userId, Channel channel) {
        synchronized (bindLocker) {
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

    public static void remove(Channel channel) {
        synchronized(removeLocker) { // 确保原子性

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
    }

    public static void remove(String userId) {
        synchronized(removeLocker) { // 确保原子性

            Channel channel = USER_CHANNEL.get(userId);
            USER_CHANNEL.remove(userId); // map
            CHANNEL_GROUP.remove(channel);

            // 关闭channel
            if (!ObjectUtils.isEmpty(channel)) {
                channel.close();
            }
        }
    }

    /**
     * 判断用户是否在线
     * map和channelGroup中均能找得到对应的channel说明用户在线
     * @return      在线就返回对应的channel，不在线返回null
     */
    public static Channel isBind(String userId) {
        Channel channel = USER_CHANNEL.get(userId); // map
        if (ObjectUtils.isEmpty(channel)) {
            return null;
        }
        return CHANNEL_GROUP.find(channel.id());
    }

    public static boolean isBind(Channel channel) {
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = channel.attr(key).get();
        return !ObjectUtils.isEmpty(userId) &&
                !ObjectUtils.isEmpty(USER_CHANNEL.get(userId));
    }

    public static void forceOffLine(String userId) {
        Channel channel = isBind(userId);
        if (!ObjectUtils.isEmpty(channel)) {
            // 推送下线通知
            MsgModel<Object> msgModel = new MsgModel<>();
            msgModel.setAction(MsgActionEnum.FORCE_OFFLINE.type);
            msgModel.setData(MsgActionEnum.FORCE_OFFLINE.content);
            pushMsg(userId, msgModel);

            // 移除通道。服务端单方面关闭连接。前端心跳会发送失败
            remove(userId);
        }
    }

    /**
     * 消息推送
     * @param receiverId
     * @param msgModel
     */
    public static void pushMsg(String receiverId, MsgModel msgModel) {
        Channel receiverChannel = isBind(receiverId);
        if (!ObjectUtils.isEmpty(receiverChannel)) {
            TextWebSocketFrame frame = new TextWebSocketFrame(toJson(msgModel));
            receiverChannel.writeAndFlush(frame);
        } else {
            // 离线状态
            log.info("{} 用户离线", receiverId);
        }
    }

    private static String toJson(MsgModel msgModel) {
        // 在线，就推送；离线，不做处理
        ObjectMapper mapper = SpringUtils.getBean(ObjectMapper.class);
        try {
            return mapper.writeValueAsString(msgModel);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static void print() {
        log.info("所有通道的长id：");
        for (Channel channel : CHANNEL_GROUP) {
            log.info(channel.id().asLongText());
        }
        log.info("userId -> channel 的映射：");
        for (Map.Entry<String, Channel> entry : USER_CHANNEL.entrySet()) {
            log.info("userId: {} ---> channelId: {}", entry.getKey(), entry.getValue().id().asLongText());
        }
    }

}
