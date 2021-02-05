package net.ysq.webchat.service;

import net.ysq.webchat.netty.entity.TextMsg;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 23:41
 */
public interface ChatMsgService {

    /**
     * 插入一条消息记录，状态为[未签收]
     * @return      msgId 主键
     */
    String saveMsg(TextMsg msg);

    /**
     * 签收消息
     * @param msgIds    要签收的消息的id，可传多个
     */
    void signMsg(String... msgIds);


}
