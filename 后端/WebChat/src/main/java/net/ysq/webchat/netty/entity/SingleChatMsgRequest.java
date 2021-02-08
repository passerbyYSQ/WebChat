package net.ysq.webchat.netty.entity;

/**
 * 单聊消息的请求格式
 * 由前端传来
 *
 * @author passerbyYSQ
 * @create 2021-02-08 15:29
 */
public class SingleChatMsgRequest {

    // 接收者的用户id
    private String receiverId;

    // 消息的具体内容
    private String content;

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
}
