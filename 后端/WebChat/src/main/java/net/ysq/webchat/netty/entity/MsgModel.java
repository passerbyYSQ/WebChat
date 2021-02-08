package net.ysq.webchat.netty.entity;

import java.io.Serializable;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 22:31
 */
public class MsgModel<T> implements Serializable {
    // 消息类型
    private Integer action;
    // 消息实体
    private T data;

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
