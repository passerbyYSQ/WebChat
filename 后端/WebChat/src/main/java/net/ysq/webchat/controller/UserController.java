package net.ysq.webchat.controller;

import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import net.ysq.webchat.po.User;
import net.ysq.webchat.service.UserService;
import net.ysq.webchat.utils.Base64Utils;
import net.ysq.webchat.utils.RedisUtils;
import net.ysq.webchat.vo.LoginReqVo;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

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

            user = userService.register(vo);
            if (ObjectUtils.isEmpty(user)) {
                return ResultModel.failed(StatusCode.PASSWORD_INCORRECT);  // 注册失败
            }
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

        Map<String, String> map = new HashMap<>();
        map.put("faceImage", user.getFaceImage());
        map.put("faceImageBig", user.getFaceImageBig());
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
