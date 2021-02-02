package net.ysq.webchat.service.impl;

import net.ysq.webchat.dao.MyFriendMapper;
import net.ysq.webchat.po.MyFriend;
import net.ysq.webchat.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author passerbyYSQ
 * @create 2021-02-03 0:46
 */
@Service // 不要忘了
public class FriendServiceImpl implements FriendService {

    @Autowired
    private MyFriendMapper myFriendMapper;

    @Override
    public MyFriend getMyOneFriend(String myId, String friendId) {
        MyFriend conds = new MyFriend();
        conds.setMyUserId(myId);
        conds.setMyFriendUserId(friendId);
        return myFriendMapper.selectOne(conds);
    }
}
