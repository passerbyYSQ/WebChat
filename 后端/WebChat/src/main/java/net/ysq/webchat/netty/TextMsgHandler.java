package net.ysq.webchat.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.ysq.webchat.netty.entity.MsgActionEnum;
import net.ysq.webchat.netty.entity.MsgModel;
import net.ysq.webchat.netty.entity.SingleChatMsgRequest;
import net.ysq.webchat.netty.entity.SingleChatMsgResponse;
import net.ysq.webchat.po.ChatMsg;
import net.ysq.webchat.service.ChatMsgService;
import net.ysq.webchat.utils.JwtUtils;
import net.ysq.webchat.utils.RedisUtils;
import net.ysq.webchat.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于处理文本消息的handler
 *
 * @author passerbyYSQ
 * @create 2021-02-05 21:23
 */
public class TextMsgHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static ChatMsgService chatMsgService;
    private final static RedisUtils redisUtils;
    private final static ObjectMapper objectMapper;

    static {
        chatMsgService = (ChatMsgService) SpringUtils.getBean("chatMsgServiceImpl");
        redisUtils = (RedisUtils) SpringUtils.getBean("redisUtils");
        objectMapper = SpringUtils.getBean(ObjectMapper.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        logger.info("接收到的文本消息：{}", msg.text());

        // 消息类型
        JsonNode rootNode = objectMapper.readTree(msg.text());
        Integer action = rootNode.get("action").asInt();
        // 取出数据部分，不同的消息类型，数据部分对应的泛型不一样
        JsonNode dataNode = rootNode.get("data");
        Channel channel = ctx.channel();

        // 判断消息类型
        // 根据不同的消息类型，处理不同的业务
        if (action.equals(MsgActionEnum.BIND.type)) {
            // 1、当websocket第一次open的时候，初始化channel，把channel和userId关联起来
            // 如果是CONNECT类型，与前端约定，data部分是token
            String token = objectMapper.treeToValue(dataNode, String.class);

            // 先验证是否过期。如果过期会抛出异常，全局捕获。之后的代码不会执行
            JwtUtils.verifyJwt(token, JwtUtils.DEFAULT_SECRET);
            // 如果没有抛出异常，表示token有效。则在Redis中寻找对应的登录信息
            String userId = JwtUtils.getClaimByKey(token, "userId");
            String redisToken = (String) redisUtils.get("user:" + userId);

            if (!StringUtils.isEmpty(redisToken) && token.equals(redisToken)) {
                UserChannelRepository.bind(userId, channel);
                // 查询是否有未签收的消息，如果有，就一次性全部推送（并不是逐条推送）
                List<SingleChatMsgResponse> unsignedMsgList = chatMsgService.getUnsignedMsg(userId);
                if (unsignedMsgList.size() > 0) {
                    MsgModel<List<SingleChatMsgResponse>> model = new MsgModel<>();
                    model.setAction(MsgActionEnum.CHAT.type);
                    model.setData(unsignedMsgList);
                    UserChannelRepository.pushMsg(userId, model);
                }
            }
            // 测试
            UserChannelRepository.print();

        } else if (action.equals(MsgActionEnum.CHAT.type)) {
            // 2、聊天类型的消息，把消息保存到数据库，同时标记消息状态为[未签收]
            SingleChatMsgRequest data = objectMapper.treeToValue(dataNode, SingleChatMsgRequest.class);
            // 由于是通过websocket，而并非http协议，所以mica-xss的拦截器没有作用。此处需要我们自己转义
            data.setContent(HtmlUtils.htmlEscape(data.getContent(), "UTF-8"));

            // 对于聊天消息，channel所绑定的user是发送者
            String senderId = UserChannelRepository.getUserId(channel);

            // 往消息表插入数据
            ChatMsg chatMsg = chatMsgService.saveMsg(senderId, data);

            // 构建消息实体
            MsgModel<List<SingleChatMsgResponse>> model = new MsgModel<>();
            model.setAction(MsgActionEnum.CHAT.type);
            List<SingleChatMsgResponse> unsignedMsgList = new ArrayList<>();
            unsignedMsgList.add(new SingleChatMsgResponse(chatMsg));
            model.setData(unsignedMsgList);

            // 推送消息
            UserChannelRepository.pushMsg(data.getReceiverId(), model);

        } else if (action.equals(MsgActionEnum.SIGNED.type)) {
            // 3、签收消息的类型。针对具体的消息进行签收，修改数据库对应的消息状态为[已签收]
            // 签收状态并非是指用户有没有读了消息。而是消息是否已经被推送到达用户的手机设备
            // 在签收类型的消息中，代表需要签收的消息的id。多个id之间用,分隔
            String msgIdsStr = objectMapper.treeToValue(dataNode, String.class);

            // 对于要签收类型消息，只有是我收到的消息，我才能签收。所以我是接收者
            String receiverId = UserChannelRepository.getUserId(channel);
            if (!StringUtils.isEmpty(msgIdsStr)) {
                String[] msgIds = msgIdsStr.split(",");
                if (!ObjectUtils.isEmpty(msgIds)) {
                    chatMsgService.signMsg(receiverId, msgIds);
                }
            }

        } else if (action.equals(MsgActionEnum.KEEP_ALIVE.type)) {
            // 4、心跳类型的消息
            // 假如客户端进程被正常退出，websocket主动断开连接，那么服务端对应的channel是会释放的
            // 但是如果客户端关闭网络后，重启网络，会导致服务端会再新建一个channel
            // 而旧的channel已经没用了，但是并没有被移除
//            logger.info("收到来自于channel {} 的心跳包", channel.id().asLongText());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserChannelRepository.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UserChannelRepository.remove(ctx.channel());
        logger.info("剩余通道个数：{}", UserChannelRepository.CHANNEL_GROUP.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        UserChannelRepository.remove(ctx.channel());
    }
}
