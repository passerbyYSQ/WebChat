package net.ysq.webchat.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Table(name = "my_friend")
public class MyFriend {
    @Id
    private String id;

    /**
     * 用户id
     */
    @Column(name = "my_user_id")
    private String myUserId;

    /**
     * 用户的好友id
     */
    @Column(name = "my_friend_user_id")
    private String myFriendUserId;

    /**
     * 备注名
     */
    @Column(name = "remarks_name")
    private String remarksName;

    /**
     * 成功添加为好友的时间
     */
    @Column(name = "added_date_time")
    private LocalDateTime addedDateTime;

}
