package net.ysq.webchat.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户id和channel关联的仓库
 *
 * @author passerbyYSQ
 * @create 2021-02-05 23:20
 */
public class UserChannelRepository {

    private static ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, Channel> USER_CHANNEL = new HashMap<>();

    public static void put(String userId, Channel channel) {
        USER_CHANNEL.put(userId, channel);
    }

    public static Channel get(String userId) {
        return USER_CHANNEL.get(userId);
    }

    public static void add(Channel channel) {
        CHANNELS.add(channel);
    }

    public static void remove(Channel channel) {
        CHANNELS.remove(channel);
    }

    public static Channel find(Channel channel) {
        return CHANNELS.find(channel.id());
    }
}
