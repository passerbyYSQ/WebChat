package net.ysq.webchat.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import net.ysq.webchat.common.PageData;
import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
    public ResultModel updateRequestStatus(@NotBlank String requestId, @Range(min = 0, max = 3) byte status,
                                           HttpServletRequest request) {

        FriendRequest friendRequest = friendRequestService.getOneFriendRequest(requestId);
        if (ObjectUtils.isEmpty(friendRequest)) { // 好友申请不存在，可能因为requestId错误
            return ResultModel.failed(StatusCode.FRIEND_REQUEST_NOT_EXIST);
        }

        String myId = (String) request.getAttribute("userId");
        if (!friendRequest.getAcceptUserId().equals(myId)) { // 没有权限修改其他人的好友申请
            return ResultModel.failed(StatusCode.NO_PERM);
        }

        // 修改数据库记录
        friendRequestService.updateRequestStatus(requestId, status);

        // netty推送
        // ...
        // 如果是忽略，则不推送；否则，推送

        return ResultModel.success();
    }

    /**
     * 我收到的好友申请的列表
     * @param page      当前页数
     * @param count     每一页显示的数量
     */
    @GetMapping("list")
    public ResultModel<PageData<FriendRequestCard>> friendRequestList(Integer page, Integer count,
                                                                      HttpServletRequest request) {
        if (ObjectUtils.isEmpty(page)) { // 如果为空，给个默认值
            page = 1;
        }
        // page 是否越界，可不需要判断，PageHelper内部会判断并纠正
        if (ObjectUtils.isEmpty(count) || count <= 0) {
            count = 10;
        }

        // 调用PageHelper进行分页
        // 紧跟在这个方法后的第一个MyBatis 查询方法会被进行分页
        PageHelper.startPage(page, count);

        String myId = (String) request.getAttribute("userId");
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
                                         HttpServletRequest request) {
        // 不能给自己发送好友申请
        String myId = (String) request.getAttribute("userId");
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

        // netty推送
        // ...

        return ResultModel.success();
    }



}
