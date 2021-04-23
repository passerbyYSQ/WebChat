package net.ysq.webchat.controller;

import net.ysq.webchat.common.Constant;
import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import net.ysq.webchat.netty.UserChannelRepository;
import net.ysq.webchat.po.User;
import net.ysq.webchat.service.UserService;
import net.ysq.webchat.utils.Base64Utils;
import net.ysq.webchat.utils.RedisUtils;
import net.ysq.webchat.vo.LoginReqVo;
import net.ysq.webchat.vo.UserCard;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author passerbyYSQ
 * @create 2021-01-28 23:12
 */
@Controller
@RequestMapping("user")
@ResponseBody
@Validated
public class UserController {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    /**
     * 搜索用户
     * @param words     用户的手机号或者用户名
     * @return
     */
    @GetMapping("search")
    public ResultModel<List<UserCard>> searchUser(@NotBlank String words,
                                                  @RequestAttribute("userId") String myId) {

        List<User> users = new ArrayList<>();
        boolean isFound = false;

        if (words.matches(Constant.REGEX_PHONE)) {
            User user = userService.getUserByPhone(words);
            if (!ObjectUtils.isEmpty(user)) {
                users.add(user);
                isFound = true; // 如果精确查找到了，就不需要模糊查询了
            }
        }

        if (!isFound) {
            // 如果无法精确查找，就模糊查询
            users = userService.getUserLikeUsername(words);
        }

        List<UserCard> userCards = users.stream()
                .filter((user) -> !user.getId().equals(myId)) // 将自己排除掉。true表示保留
                .map(UserCard::new)
                .collect(Collectors.toList());

        return ResultModel.success(userCards);
    }

    /**
     * 退出登录
     */
    @PostMapping("logout")
    public ResultModel logout(@RequestAttribute("userId") String userId) {
//        logger.info("logout, userId={}", userId);
        redisUtils.del("user:" + userId);
        // 移除通道
        UserChannelRepository.remove(userId);
        return ResultModel.success();
    }

    /**
     * 登录
     */
    @PostMapping("login")
    public ResultModel<User> login(@Validated LoginReqVo vo) {
        // 根据手机号查找用户
        User user = userService.getUserByPhone(vo.getPhone());
        if (ObjectUtils.isEmpty(user)) {  // 尚未注册
            user = userService.register(vo); // 注册成功后重新赋值
        } else {  // 已注册
            String md5Pwd = DigestUtils.md5DigestAsHex(vo.getPassword().getBytes());
            if (!md5Pwd.equals(user.getPassword())) {
                return ResultModel.failed(StatusCode.PASSWORD_INCORRECT); // 密码错误
            }
        }

        // 登录成功之后的操作

        // 检测redis中是否该账号的token，如果有，表示该账号已经被人登录了
        if (!ObjectUtils.isEmpty(redisUtils.get("user:"+ user.getId()))) {
            // 给对方推送下线通知
            UserChannelRepository.forceOffLine(user.getId());
        }

        // 签发token
        String token = userService.generateAndSaveToken(user.getId());
        user.setToken(token);
        return ResultModel.success(user);
    }

    /**
     * 修改用户头像
     * @param base64
     * @return
     */
    @PostMapping("faceImg")
    public ResultModel<Map<String, String>> updateFaceImg(
            @NotBlank @Pattern(regexp = Base64Utils.REGEX) String base64,
            @RequestAttribute("userId") String userId) {

        User user = userService.uploadAndUpdateFaceImg(base64, userId);

        Map<String, String> map = new HashMap<>();
        map.put("faceImage", user.getFaceImage());
        map.put("faceImageBig", user.getFaceImageBig());
        map.put("qrcode", user.getQrcode());
        return ResultModel.success(map);
    }

    /**
     * 修改用户名
     */
    @PostMapping("username")
    public ResultModel updateUsername(@NotBlank @Length(min = 2, max = 20) String username,
                                      @RequestAttribute("userId") String userId) {
        User user = userService.getUserByUsername(username);
        if (!ObjectUtils.isEmpty(user)) {
            return ResultModel.failed(StatusCode.USERNAME_IS_EXIST); // 用户名已经存在
        }

        user = new User();
        user.setId(userId);
        user.setUsername(username);
        userService.updateUserById(user); // 修改

        return ResultModel.success();
    }

}
