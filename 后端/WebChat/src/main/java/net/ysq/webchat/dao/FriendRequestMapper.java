package net.ysq.webchat.dao;

import net.ysq.webchat.common.BaseMapper;
import net.ysq.webchat.po.FriendRequest;
import net.ysq.webchat.vo.FriendRequestCard;

import java.util.List;

public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    /**
     * 查询我收到的好友申请的列表
     */
    List<FriendRequestCard> getFriendRequestList(String receiverId);
}
