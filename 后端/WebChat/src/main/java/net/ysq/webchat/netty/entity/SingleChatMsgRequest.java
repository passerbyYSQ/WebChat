package net.ysq.webchat.netty.entity;

import lombok.Data;

/**
 * 单聊消息的请求格式
 * 由前端传来
 *
 * @author passerbyYSQ
 * @create 2021-02-08 15:29
 */
@Data
public class SingleChatMsgRequest {

    // 接收者的用户id
    private String receiverId;

    // 消息的具体内容
    private String content;

}
