package net.ysq.webchat.dao;

import net.ysq.webchat.common.BaseMapper;
import net.ysq.webchat.netty.entity.SingleChatMsgResponse;
import net.ysq.webchat.po.ChatMsg;

import java.util.List;
import java.util.Map;

public interface ChatMsgMapper extends BaseMapper<ChatMsg> {

    /**
     * 查询所有未签收的消息
     */
    List<SingleChatMsgResponse> getUnsignedMsg(String receiverId);

    /**
     * 批量签收
     */
    int batchUpdateMsgSigned(Map<String, Object> params);

}
