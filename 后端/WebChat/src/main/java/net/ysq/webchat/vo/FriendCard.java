package net.ysq.webchat.vo;

import lombok.Data;

/**
 * 用于显示好友列表
 * @author passerbyYSQ
 * @create 2021-02-04 14:29
 */
@Data
public class FriendCard {

    private String userId; // 注意，这里是userId，而不是MyFriend表的主键id
    private String username;
    private String remarksName; // 备注名
    private String faceImg; // 小头像

}
