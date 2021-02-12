package net.ysq.webchat.service.impl;

import net.ysq.webchat.dao.ChatMsgMapper;
import net.ysq.webchat.netty.entity.MsgSignFlagEnum;
import net.ysq.webchat.netty.entity.SingleChatMsgRequest;
import net.ysq.webchat.netty.entity.SingleChatMsgResponse;
import net.ysq.webchat.po.ChatMsg;
import net.ysq.webchat.service.ChatMsgService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 23:43
 */
@Service
public class ChatMsgServiceImpl implements ChatMsgService {

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private Sid sid;

    @Override
    public List<SingleChatMsgResponse> getUnsignedMsg(String receiverId) {
        return chatMsgMapper.getUnsignedMsg(receiverId);
    }

    @Override
    public ChatMsg saveMsg(String senderId, SingleChatMsgRequest req) {
        ChatMsg record = new ChatMsg();
        record.setId(sid.nextShort()); // 消息的主键id
        record.setSendUserId(senderId);
        record.setAcceptUserId(req.getReceiverId());
        String content = req.getContent()
                .replaceAll("(\\r|\\n|\\r\\n)", "<br/>");
        record.setMsg(content); // 消息内容
        record.setCreateTime(LocalDateTime.now());
        record.setSignFlag(MsgSignFlagEnum.UNSIGNED.type); // 状态为[未签收]
        chatMsgMapper.insert(record);
        return record; // 将消息id返回，用于前端请求签收
    }

    @Override
    public void signMsg(String receiverId, String... msgIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("receiverId", receiverId);
        List<String> msgIdList = Arrays.asList(msgIds);
        params.put("msgIdList", msgIdList);
        chatMsgMapper.batchUpdateMsgSigned(params);
    }
}
