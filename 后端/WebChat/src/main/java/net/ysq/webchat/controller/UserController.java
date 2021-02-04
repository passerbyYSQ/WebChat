package net.ysq.webchat.controller;

import net.ysq.webchat.common.Constant;
import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import net.ysq.webchat.po.User;
import net.ysq.webchat.service.UserService;
import net.ysq.webchat.utils.Base64Utils;
import net.ysq.webchat.utils.RedisUtils;
import net.ysq.webchat.vo.LoginReqVo;
import net.ysq.webchat.vo.UserCard;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    public ResultModel<List<UserCard>> searchUser(@NotBlank String words, HttpServletRequest request) {
        if (words.matches(Constant.REGEX_PHONE)) {
            // 根据手机号精确查询
            User user = userService.getUserByPhone(words);
            if (!ObjectUtils.isEmpty(user)) {
                List<UserCard> userCards = new ArrayList<>();
                userCards.add(new UserCard(user));
                return ResultModel.success(userCards);
            }
        }

        String myId = (String) request.getAttribute("userId");

        // 如果不是手机号，或者手机号查找不到用户，就模糊查询用户名
        List<User> users = userService.getUserLikeUsername(words);
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
    public ResultModel logout(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
//        logger.info("logout, userId={}", userId);
        redisUtils.del("user:" + userId);
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
//            if (ObjectUtils.isEmpty(user)) {
//                return ResultModel.failed(StatusCode.PASSWORD_INCORRECT);  // 注册失败
//            }
        } else {  // 已注册
            String md5Pwd = DigestUtils.md5DigestAsHex(vo.getPassword().getBytes());
            if (!md5Pwd.equals(user.getPassword())) {
                return ResultModel.failed(StatusCode.PASSWORD_INCORRECT); // 密码错误
            }
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
    public ResultModel<Map<String, String>> updateFaceImg(HttpServletRequest request,
            @NotBlank @Pattern(regexp = Base64Utils.REGEX) String base64) {

        String userId = (String) request.getAttribute("userId");

        User user = userService.uploadAndUpdateFaceImg(base64, userId);

        //long timestamp = Instant.now().toEpochMilli(); // 获取当前时间戳，拼接到图片url后面
        // 防止因为前端网页因为缓存不请求同一路径的图片
        Map<String, String> map = new HashMap<>(); //  + "&rand=" + timestamp
        map.put("faceImage", user.getFaceImage());
        map.put("faceImageBig", user.getFaceImageBig());
        map.put("qrcode", user.getQrcode());
        return ResultModel.success(map);
    }

    /**
     * 修改用户名
     */
    @PostMapping("username")
    public ResultModel updateUsername(@NotBlank @Length(min = 2, max = 32) String username,
                                      HttpServletRequest request) {
        User user = userService.getUserByUsername(username);
        if (!ObjectUtils.isEmpty(user)) {
            return ResultModel.failed(StatusCode.USERNAME_IS_EXIST); // 用户名已经存在
        }

        String userId = (String) request.getAttribute("userId");
        user = new User();
        user.setId(userId);
        user.setUsername(username);
        userService.updateUserById(user); // 修改

        return ResultModel.success();
    }

}
