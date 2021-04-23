package net.ysq.webchat.netty.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 22:31
 */
@Data
public class MsgModel<T> implements Serializable {
    // 消息类型
    private Integer action;
    // 消息实体
    private T data;
}
