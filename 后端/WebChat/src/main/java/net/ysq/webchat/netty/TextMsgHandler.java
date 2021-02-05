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
import net.ysq.webchat.utils.SpringUtils;
import org.springframework.util.ObjectUtils;

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
        String senderId = textMsg.getSenderId();
        String receiverId = textMsg.getReceiverId();
        Channel channel = ctx.channel();
        ChatMsgService chatMsgService = (ChatMsgService) SpringUtils.getBean("chatMsgServiceImpl");
        // 判断消息类型
        // 根据不同的消息类型，处理不同的业务
        if (action.equals(MsgActionEnum.CONNECT.type)) {
            // 1、当websocket第一次open的时候，初始化channel，把channel和userId关联起来
            // 关联用户id和channel
            UserChannelRepository.put(senderId, channel);

        } else if (action.equals(MsgActionEnum.CHAT.type)) {
            // 2、聊天类型的消息，把消息保存到数据库，同时标记消息状态为[未签收]
            // 将消息保存到数据库
            String msgId = chatMsgService.saveMsg(textMsg);
            textMsg.setMsgId(msgId);

            // 推送消息
            TextMsgModel model = new TextMsgModel();
            model.setAction(MsgActionEnum.CHAT.type);
            model.setMsg(textMsg);
            // 获取接收者的channel
            Channel receiverChannel = UserChannelRepository.get(receiverId);
            if (ObjectUtils.isEmpty(receiverChannel)) {
                // 如果是空，表示尚未关联。可能channel已经创建了，但是还没有发送CONNECT类型消息，使userId和channel关联
                // 离线
            } else {
                // 如果不为空，去channelGroup里面查找对应的channel是否存在
                Channel foundChannel = UserChannelRepository.find(receiverChannel);
                if (ObjectUtils.isEmpty(foundChannel)) { // 离线
                    // 用户离线。channelGroup中的对应的channel被移除了。但是map中的userId--channel的键值对没被移除
                } else { // 非空，表示在线
                    // 推送
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(model)));
                }
            }
        } else if (action.equals(MsgActionEnum.SIGNED.type)) {
            // 3、签收消息的类型。针对具体的消息进行签收，修改数据库对应的消息状态为[已签收]
            // 签收状态并非是指用户有没有读了消息。而是消息是否已经被推送到达用户的手机设备
            // 扩展字段在签收类型的消息中，代表需要签收的消息的id。多个id之间用,分隔
            String[] msgIds = msgModel.getExtend().split(",");
            if (!ObjectUtils.isEmpty(msgIds)) {
                chatMsgService.signMsg(msgIds);
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
