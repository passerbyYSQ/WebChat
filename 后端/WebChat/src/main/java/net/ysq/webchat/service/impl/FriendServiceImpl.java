package net.ysq.webchat.service.impl;

import net.ysq.webchat.dao.MyFriendMapper;
import net.ysq.webchat.po.MyFriend;
import net.ysq.webchat.service.FriendService;
import net.ysq.webchat.vo.FriendCard;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2021-02-03 0:46
 */
@Service // 不要忘了
public class FriendServiceImpl implements FriendService {

    @Autowired
    private MyFriendMapper myFriendMapper;

    @Autowired
    private Sid sid;

    @Override
    public List<FriendCard> getFriendList(String myId) {
        return myFriendMapper.getFriendList(myId);
    }

    @Transactional
    @Override
    public void addFriend(String myId, String friendId) {
        // 往好友列表插入数据
        MyFriend record = new MyFriend();
        record.setId(sid.nextShort());
        record.setMyUserId(myId);
        record.setMyFriendUserId(friendId); // 对于我来说，对方是我好友
        record.setAddedDateTime(LocalDateTime.now());
        myFriendMapper.insert(record); // 添加好友记录。相互的，所以需要插入两条

        record.setId(sid.nextShort());
        record.setMyUserId(friendId);
        record.setMyFriendUserId(myId); // 对于对方来说，我是他的好友
        myFriendMapper.insert(record);
    }

    @Override
    public MyFriend getMyOneFriend(String myId, String friendId) {
        MyFriend conds = new MyFriend();
        conds.setMyUserId(myId);
        conds.setMyFriendUserId(friendId);
        return myFriendMapper.selectOne(conds);
    }
}
