package net.ysq.webchat.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import net.ysq.webchat.common.PageData;
import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import net.ysq.webchat.netty.UserChannelRepository;
import net.ysq.webchat.netty.entity.MsgActionEnum;
import net.ysq.webchat.netty.entity.MsgModel;
import net.ysq.webchat.po.FriendRequest;
import net.ysq.webchat.po.MyFriend;
import net.ysq.webchat.po.User;
import net.ysq.webchat.service.FriendRequestService;
import net.ysq.webchat.service.FriendService;
import net.ysq.webchat.service.UserService;
import net.ysq.webchat.vo.FriendRequestCard;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2021-02-02 14:54
 */
@Controller
@ResponseBody
@RequestMapping("friendRequest")
@Validated
public class FriendRequestController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestService friendRequestService;

    /**
     * 处理我收到的好友申请
     */
    @PostMapping("process")
    public ResultModel<String> updateRequestStatus(@NotBlank String requestId, @Range(min = 0, max = 3) byte status,
                                                   @RequestAttribute("userId") String myId) {
        // requestId合法性检查
        FriendRequest friendRequest = friendRequestService.getOneFriendRequest(requestId);
        if (ObjectUtils.isEmpty(friendRequest)) { // 好友申请不存在，可能因为requestId错误
            return ResultModel.failed(StatusCode.FRIEND_REQUEST_NOT_EXIST);
        }

        // 已处理过的，禁止重复处理
        if (friendRequest.getStatus() > 0) {
            return ResultModel.failed(StatusCode.REPEAT_PROCESS);
        }

        // 没有权限修改其他人的好友申请
        if (!friendRequest.getAcceptUserId().equals(myId)) {
            return ResultModel.failed(StatusCode.NO_PERM);
        }

        // 如果已经是好友，就无法发送申请
        // 情景：都发送了申请，一方同意之后，另一方又同意
        // A发给B（申请为p1），B也发给了A（p2）。B先同意了（p1）。后来A又同意（p2）
        // 这个时候我们需要给A提示说，你们已经是好友了。同时帮A将那条无法处理的申请的状态改为同意
        String senderId = friendRequest.getSendUserId();
        MyFriend friend = friendService.getMyOneFriend(myId, senderId);
        if (!ObjectUtils.isEmpty(friend)) {
            friendRequestService.updateRequestStatus(requestId, (byte) 1);
            return ResultModel.failed(StatusCode.HAVE_BEEN_FRIEND);
        }

        // 修改数据库记录。如果是同意，还需要往好友列表插入一条数据
        friendRequestService.agreeFriendRequest(requestId, status, myId, senderId);

        // 0：尚未处理；1：同意；2：忽略；3：拒绝
        // netty推送
        // 如果是忽略或者拒绝，则不推送；如果是同意，则推送通知
        if (status == 1) {
            MsgModel<String> model = new MsgModel<>();
            model.setAction(MsgActionEnum.PULL_FRIEND.type);
            model.setData(myId); // 把我的id推给发送者
            // 我处理请求，说明我是这条申请的接收者。需要推送给申请的发送者
            UserChannelRepository.pushMsg(senderId, model);
        }

        return ResultModel.success(senderId);  // 返回给我发送者的id
    }

    /**
     * 我收到的好友申请的列表
     * @param page      当前页数
     * @param count     每一页显示的数量
     */
    @GetMapping("list")
    public ResultModel<PageData<FriendRequestCard>> friendRequestList(
            @RequestParam(defaultValue = "1") Integer page, Integer count,
            @RequestAttribute("userId") String myId) {

        // page 是否越界，可不需要判断，PageHelper内部会判断并纠正
        if (ObjectUtils.isEmpty(count) || count <= 0) {
            count = 10;
        }

        // 调用PageHelper进行分页
        // 紧跟在这个方法后的第一个MyBatis 查询方法会被进行分页
        PageHelper.startPage(page, count);

        // 查询
        List<FriendRequestCard> friendRequestList = friendRequestService.getFriendRequestList(myId);

        // 获取各种分页属性
        PageInfo<FriendRequestCard> pageInfo = new PageInfo<>(friendRequestList);

        // 封装返回的数据
        PageData<FriendRequestCard> pageData = new PageData<>
                (pageInfo.getPageNum(), count, pageInfo.getTotal(), friendRequestList);

        return ResultModel.success(pageData);
    }

    /**
     * 发送好友申请
     */
    @PostMapping("send")
    public ResultModel sendFriendRequest(@NotBlank String receiverId, String content,
                                         @RequestAttribute("userId") String myId) {
        // 不能给自己发送好友申请
        if (myId.equals(receiverId)) {
            return ResultModel.failed(StatusCode.CAN_NOT_ADD_SELF);
        }

        // 判断userId是否合法
        User receiver = userService.getUserById(receiverId);
        if (ObjectUtils.isEmpty(receiver)) {
            return ResultModel.failed(StatusCode.USER_NOT_EXIST);
        }

        // 如果已经是好友，就无法发送申请
        MyFriend friend = friendService.getMyOneFriend(myId, receiver.getId());
        if (!ObjectUtils.isEmpty(friend)) {
            return ResultModel.failed(StatusCode.HAVE_BEEN_FRIEND);
        }

        FriendRequest friendRequest = friendRequestService.getOneFriendRequest(myId, receiver.getId());
        if (ObjectUtils.isEmpty(friendRequest)) {  // 还没有发送过申请
            // 插入一条记录
            friendRequestService.addOneFriendRequest(myId, receiver.getId(), content);
        } else {
            // 之前已经申请过了。可能对方：忽略或者拒绝了。现在重新再次申请
            friendRequestService.reSendFriendRequest(friendRequest.getId(), content);
        }

        // netty推送好友申请
        MsgModel msgModel = new MsgModel();
        msgModel.setAction(MsgActionEnum.FRIEND_REQUEST.type);
        UserChannelRepository.pushMsg(receiver.getId(), msgModel);

        return ResultModel.success();
    }



}
