package net.ysq.webchat.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
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

}
