package net.ysq.webchat.service.impl;

import net.ysq.webchat.dao.FriendRequestMapper;
import net.ysq.webchat.po.FriendRequest;
import net.ysq.webchat.service.FriendRequestService;
import net.ysq.webchat.service.FriendService;
import net.ysq.webchat.vo.FriendRequestCard;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2021-02-02 15:00
 */
@Service // 不要忘了
public class FriendRequestServiceImpl implements FriendRequestService {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private Sid sid;

    /**
     * agreeFriendRequest()上加了@Transactional，
     * addFriend()也加了@Transactional。
     * 会不会addFriend的事务跟agreeFriendRequest的事务会不会不是同一个？
     */
    @Transactional // 注意这里加上事务注解
    @Override
    public void agreeFriendRequest(String requestId, byte status, String myId, String friendId) {
        updateRequestStatus(requestId, status);
        if (status == 1) { // 同意
            // 注意addFriend这里面不需要重复加@Transactional了。因为默认的事务传播级别为REQUIRED（必须的）
            // 子方法如果是必须运行在一个事务中的，如果当前存在事务，则加入这个事务，成为一个整体。如果当前没有事务，则自己新建一个事务，
            friendService.addFriend(myId, friendId);
        }
    }

    @Override
    public void updateRequestStatus(String requestId, byte status) {
        FriendRequest request = new FriendRequest();
        request.setId(requestId);
        request.setStatus(status);
        // 更新好友申请的状态。注意使用selective
        friendRequestMapper.updateByPrimaryKeySelective(request);
    }

    // 由于涉及到联表查询，不适合用tk.mapper。所以自己手写sql
    @Override
    public List<FriendRequestCard> getFriendRequestList(String receiverId) {
        return friendRequestMapper.getFriendRequestList(receiverId);
    }

    @Override
    public FriendRequest getOneFriendRequest(String senderId, String receiverId) {
        FriendRequest conds = new FriendRequest();
        conds.setSendUserId(senderId);
        conds.setAcceptUserId(receiverId);
        return friendRequestMapper.selectOne(conds);
    }

    @Override
    public FriendRequest getOneFriendRequest(String requestId) {
        return friendRequestMapper.selectByPrimaryKey(requestId);
    }

    @Override
    public void reSendFriendRequest(String requestId, String content) {
        FriendRequest request = new FriendRequest();
        request.setId(requestId);
        request.setContent(content);
        request.setStatus((byte) 0); // 注意，状态重置为：尚未处理
        request.setRequestDateTime(LocalDateTime.now()); // 更新发送申请的时间
        friendRequestMapper.updateByPrimaryKeySelective(request); // 注意使用selective
    }


    @Override
    public void addOneFriendRequest(String senderId, String receiverId, String content) {
        FriendRequest request = new FriendRequest();
        request.setId(sid.nextShort());
        request.setSendUserId(senderId);
        request.setAcceptUserId(receiverId);
        request.setContent(content);
        request.setStatus((byte) 0); // 状态为尚未处理
        request.setRequestDateTime(LocalDateTime.now());
        friendRequestMapper.insert(request);
    }


}
