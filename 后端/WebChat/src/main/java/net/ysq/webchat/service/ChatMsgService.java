package net.ysq.webchat.service;

import net.ysq.webchat.netty.entity.SingleChatMsgRequest;
import net.ysq.webchat.netty.entity.SingleChatMsgResponse;
import net.ysq.webchat.po.ChatMsg;

import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 23:41
 */
public interface ChatMsgService {

    /**
     * 查询所有未签收的消息
     */
    List<SingleChatMsgResponse> getUnsignedMsg(String receiverId);

    /**
     * 插入一条消息记录，状态为[未签收]
     * @return      msgId 主键
     */
    ChatMsg saveMsg(String senderId, SingleChatMsgRequest req);

    /**
     * 签收消息
     * @param msgIds    要签收的消息的id，可传多个，用","分隔
     */
    void signMsg(String receiverId, String... msgIds);


}
