package net.ysq.webchat.service.impl;

import net.ysq.webchat.dao.ChatMsgMapper;
import net.ysq.webchat.netty.entity.MsgSignFlagEnum;
import net.ysq.webchat.netty.entity.TextMsg;
import net.ysq.webchat.po.ChatMsg;
import net.ysq.webchat.service.ChatMsgService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    public String saveMsg(TextMsg msg) {
        ChatMsg record = new ChatMsg();
        record.setId(sid.nextShort()); // 消息的主键id
        record.setSendUserId(msg.getSenderId());
        record.setAcceptUserId(msg.getReceiverId());
        record.setMsg(msg.getContent()); // 消息内容
        record.setCreateTime(LocalDateTime.now());
        record.setSignFlag(MsgSignFlagEnum.UNSIGNED.type); // [未签收]
        chatMsgMapper.insert(record);
        return record.getId();
    }

    @Override
    public void signMsg(String... msgIds) {
        List<String> msgIdList = Arrays.asList(msgIds);
        chatMsgMapper.batchUpdateMsgSigned(msgIdList);
    }
}
