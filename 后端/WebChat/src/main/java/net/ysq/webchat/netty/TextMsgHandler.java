package net.ysq.webchat.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.ysq.webchat.netty.entity.MsgActionEnum;
import net.ysq.webchat.netty.entity.TextMsg;
import net.ysq.webchat.netty.entity.TextMsgModel;
import net.ysq.webchat.service.ChatMsgService;
import net.ysq.webchat.utils.JsonUtils;
import net.ysq.webchat.utils.JwtUtils;
import net.ysq.webchat.utils.RedisUtils;
import net.ysq.webchat.utils.SpringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于处理文本消息的handler
 *
 * @author passerbyYSQ
 * @create 2021-02-05 21:23
 */
public class TextMsgHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println("接收到的文本消息：" + msg.text());

        TextMsgModel msgModel = JsonUtils.jsonToObj(msg.text(), TextMsgModel.class);
        Integer action = msgModel.getAction();
        TextMsg textMsg = msgModel.getMsg();
        Channel channel = ctx.channel();
        ChatMsgService chatMsgService = (ChatMsgService) SpringUtils.getBean("chatMsgServiceImpl");
        RedisUtils redisUtils = (RedisUtils) SpringUtils.getBean("redisUtils");
        // 判断消息类型
        // 根据不同的消息类型，处理不同的业务
        if (action.equals(MsgActionEnum.CONNECT.type)) {
            // 1、当websocket第一次open的时候，初始化channel，把channel和userId关联起来
            // 关联用户id和channel
            String token = msgModel.getExtend();
            // 先验证是否过期。如果过期会抛出异常，全局捕获。之后的代码不会执行
            JwtUtils.verifyJwt(token, JwtUtils.DEFAULT_SECRET);
            // 如果没有抛出异常，表示token有效。则在Redis中寻找对应的登录信息
            String userId = JwtUtils.getClaimByKey(token, "userId");
            String redisToken = (String) redisUtils.get("user:" + userId);
            if (!StringUtils.isEmpty(redisToken) && token.equals(redisToken)) {
                UserChannelRepository.bind(userId, channel);
                // 查询是否有未签收的消息，如果有，就推送

            }
            // 测试
            UserChannelRepository.print();

        } else if (action.equals(MsgActionEnum.CHAT.type)) {
            // 2、聊天类型的消息，把消息保存到数据库，同时标记消息状态为[未签收]
            // 将消息保存到数据库
            String msgId = chatMsgService.saveMsg(textMsg);
            textMsg.setMsgId(msgId);

            // 构建消息实体
            TextMsgModel model = new TextMsgModel();
            model.setAction(MsgActionEnum.CHAT.type);
            model.setMsg(textMsg);

            // 推送消息
            UserChannelRepository.pushMsg(textMsg.getReceiverId(), model);

        } else if (action.equals(MsgActionEnum.SIGNED.type)) {
            // 3、签收消息的类型。针对具体的消息进行签收，修改数据库对应的消息状态为[已签收]
            // 签收状态并非是指用户有没有读了消息。而是消息是否已经被推送到达用户的手机设备
            // 扩展字段在签收类型的消息中，代表需要签收的消息的id。多个id之间用,分隔
            String msgIdsStr = msgModel.getExtend();
            if (!StringUtils.isEmpty(msgIdsStr)) {
                String[] msgIds = msgIdsStr.split(",");
                if (!ObjectUtils.isEmpty(msgIds)) {
                    chatMsgService.signMsg(msgIds);
                }
            }
        } else if (action.equals(MsgActionEnum.KEEPALIVE.type)) {
            // 4、心跳类型的消息
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserChannelRepository.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UserChannelRepository.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常后关闭连接，并将channel从users中移除
        UserChannelRepository.remove(ctx.channel());
    }
}
