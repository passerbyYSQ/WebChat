package net.ysq.webchat.netty.entity;

import java.io.Serializable;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 22:35
 */
public class TextMsg implements Serializable {
    // 发送者的id
    private String senderId;
    // 接收者的用户id。如果以后扩展做群聊，则可能会有多个接收者，但发送者只有一个
    private String receiverId;
//    private List<String> receiverIds;
    // 消息的内容
    private String content;
    // 消息的id。用于消息的签收
    private String msgId;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
