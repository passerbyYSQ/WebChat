package net.ysq.webchat.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用于显示我收到的好友申请的列表
 *
 * @author passerbyYSQ
 * @create 2021-02-02 23:13
 */
@Data
public class FriendRequestCard {

    private String id; // 申请的id
    private String senderId; // 发送者的用户id
    private String senderUsername; // 发送者的用户名
    private String senderFaceImg;  // 发送者的头像
    private String content; // 备注内容
    private byte status; // 状态
    private LocalDateTime requestTime;

}
