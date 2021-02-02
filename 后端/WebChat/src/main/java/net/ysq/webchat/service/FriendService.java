package net.ysq.webchat.service;

import net.ysq.webchat.po.MyFriend;

/**
 * @author passerbyYSQ
 * @create 2021-02-03 0:46
 */
public interface FriendService {
    /**
     * 查找我的一个好友
     */
    MyFriend getMyOneFriend(String myId, String friendId);
}
