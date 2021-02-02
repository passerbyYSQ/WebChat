package net.ysq.webchat.service;

import net.ysq.webchat.po.FriendRequest;
import net.ysq.webchat.vo.FriendRequestCard;

import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2021-02-02 15:00
 */
public interface FriendRequestService {

    /**
     * 好友申请的状态
     */
    void updateRequestStatus(String requestId, byte status);

    /**
     * 查询我收到的好友申请的列表
     */
    List<FriendRequestCard> getFriendRequestList(String receiverId);

    /**
     * 查询sender给receiver发送的好友申请（只有一条，重复申请会覆盖）
     */
    FriendRequest getOneFriendRequest(String senderId, String receiverId);

    /**
     * 根据id获取好友请求
     */
    FriendRequest getOneFriendRequest(String requestId);

    /**
     * 重新发送申请
     */
    void reSendFriendRequest(String requestId, String content);

    /**
     * 添加一条申请记录
     */
    void addOneFriendRequest(String senderId, String receiverId, String content);



}
