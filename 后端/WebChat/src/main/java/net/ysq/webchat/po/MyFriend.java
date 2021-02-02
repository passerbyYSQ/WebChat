package net.ysq.webchat.po;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

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
     * 获取用户id
     *
     * @return my_user_id - 用户id
     */
    public String getMyUserId() {
        return myUserId;
    }

    /**
     * 设置用户id
     *
     * @param myUserId 用户id
     */
    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    /**
     * 获取用户的好友id
     *
     * @return my_friend_user_id - 用户的好友id
     */
    public String getMyFriendUserId() {
        return myFriendUserId;
    }

    /**
     * 设置用户的好友id
     *
     * @param myFriendUserId 用户的好友id
     */
    public void setMyFriendUserId(String myFriendUserId) {
        this.myFriendUserId = myFriendUserId;
    }

    /**
     * 获取备注名
     *
     * @return remarks_name - 备注名
     */
    public String getRemarksName() {
        return remarksName;
    }

    /**
     * 设置备注名
     *
     * @param remarksName 备注名
     */
    public void setRemarksName(String remarksName) {
        this.remarksName = remarksName;
    }

    /**
     * 获取成功添加为好友的时间
     *
     * @return added_date_time - 成功添加为好友的时间
     */
    public LocalDateTime getAddedDateTime() {
        return addedDateTime;
    }

    /**
     * 设置成功添加为好友的时间
     *
     * @param addedDateTime 成功添加为好友的时间
     */
    public void setAddedDateTime(LocalDateTime addedDateTime) {
        this.addedDateTime = addedDateTime;
    }
}
