package net.ysq.webchat.controller;

import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import net.ysq.webchat.po.MyFriend;
import net.ysq.webchat.po.User;
import net.ysq.webchat.service.FriendService;
import net.ysq.webchat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

/**
 * @author passerbyYSQ
 * @create 2021-02-03 0:45
 */
@Controller
@ResponseBody
@RequestMapping("friend")
@Validated
public class FriendController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendService friendService;

    @GetMapping("isFriend")
    public ResultModel<Boolean> isMyFriend(@NotBlank String userId, HttpServletRequest request) {
        // 判断userId是否合法
        User user = userService.getUserById(userId);
        if (ObjectUtils.isEmpty(user)) {
            return ResultModel.failed(StatusCode.USER_NOT_EXIST);
        }

        boolean isFriend;
        String myId = (String) request.getAttribute("userId");
        if (myId.equals(user.getId())) {
            isFriend = true; // 自己是自己的好友。不可添加
        } else {
            MyFriend friend = friendService.getMyOneFriend(myId, user.getId());
            isFriend = !ObjectUtils.isEmpty(friend);
        }
        return ResultModel.success(isFriend);
    }
}
