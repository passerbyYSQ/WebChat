package net.ysq.webchat.netty.entity;

import java.io.Serializable;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 22:31
 */
public class TextMsgModel implements Serializable {
    // 消息类型
    private Integer action;
    // 消息实体
    private TextMsg msg;
    // 扩展字段
    private String extend;

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public TextMsg getMsg() {
        return msg;
    }

    public void setMsg(TextMsg msg) {
        this.msg = msg;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
