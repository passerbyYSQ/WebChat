package net.ysq.webchat.vo;

import java.time.LocalDateTime;

/**
 * 用于显示我收到的好友申请的列表
 *
 * @author passerbyYSQ
 * @create 2021-02-02 23:13
 */
public class FriendRequestCard {

    private String id; // 申请的id
    private String senderUsername; // 发送者的用户名
    private String senderFaceImg;  // 发送者的头像
    private String content; // 备注内容
    private byte status; // 状态
    private LocalDateTime requestTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderFaceImg() {
        return senderFaceImg;
    }

    public void setSenderFaceImg(String senderFaceImg) {
        this.senderFaceImg = senderFaceImg;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
}
