package net.ysq.webchat.dao;

import net.ysq.webchat.common.BaseMapper;
import net.ysq.webchat.po.MyFriend;
import net.ysq.webchat.vo.FriendCard;

import java.util.List;

public interface MyFriendMapper extends BaseMapper<MyFriend> {

    /**
     * 获取好友列表
     */
    List<FriendCard> getFriendList(String myId);

}
