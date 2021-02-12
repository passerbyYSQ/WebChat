package net.ysq.webchat.netty.entity;

import net.ysq.webchat.po.ChatMsg;

import java.time.LocalDateTime;

/**
 * 单聊消息的响应格式
 * 由服务端返回给前端
 *
 * @author passerbyYSQ
 * @create 2021-02-08 15:43
 */
public class SingleChatMsgResponse {

    private String senderId;

    // 用于签收
    private String msgId;

    // 消息的具体内容
    private String content;

    private LocalDateTime createTime;

    public SingleChatMsgResponse() {
    }

    public SingleChatMsgResponse(ChatMsg chatMsg) {
        this.senderId = chatMsg.getSendUserId();
        this.msgId = chatMsg.getId();
        this.content = chatMsg.getMsg();
        this.createTime = chatMsg.getCreateTime();
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
