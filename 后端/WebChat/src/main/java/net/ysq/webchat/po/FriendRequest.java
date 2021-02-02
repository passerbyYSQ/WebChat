package net.ysq.webchat.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Table(name = "friend_request")
public class FriendRequest {
    @Id
    private String id;

    @Column(name = "send_user_id")
    private String sendUserId;

    @Column(name = "accept_user_id")
    private String acceptUserId;

    private String content;

    /**
     * 0：尚未处理；1：同意；2：忽略；3：拒绝
     */
    private Byte status;

    /**
     * 发送请求的时间
     */
    @Column(name = "request_date_time")
    private LocalDateTime requestDateTime;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return send_user_id
     */
    public String getSendUserId() {
        return sendUserId;
    }

    /**
     * @param sendUserId
     */
    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    /**
     * @return accept_user_id
     */
    public String getAcceptUserId() {
        return acceptUserId;
    }

    /**
     * @param acceptUserId
     */
    public void setAcceptUserId(String acceptUserId) {
        this.acceptUserId = acceptUserId;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取0：拒绝；1：同意；2：忽略
     *
     * @return status - 0：拒绝；1：同意；2：忽略
     */
    public Byte getStatus() {
        return status;
    }

    /**
     * 设置0：拒绝；1：同意；2：忽略
     *
     * @param status 0：拒绝；1：同意；2：忽略
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * 获取发送请求的时间
     *
     * @return request_date_time - 发送请求的时间
     */
    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    /**
     * 设置发送请求的时间
     *
     * @param requestDateTime 发送请求的时间
     */
    public void setRequestDateTime(LocalDateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }
}
