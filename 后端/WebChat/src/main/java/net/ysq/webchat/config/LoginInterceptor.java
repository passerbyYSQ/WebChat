package net.ysq.webchat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import net.ysq.webchat.service.UserService;
import net.ysq.webchat.utils.DateTimeUtils;
import net.ysq.webchat.utils.JwtUtils;
import net.ysq.webchat.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author passerbyYSQ
 * @create 2021-01-30 20:56
 */
@Component // 加入IOC容器中，以用于在配置类中注册拦截器
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    private HttpServletResponse response;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        this.response = response;

        String token = tryGetToken(request);
        if (StringUtils.isEmpty(token)) {
            // 没有携带token
            writeJson(ResultModel.failed(StatusCode.TOKEN_IS_MISSING));
            return false;
        } else {
            // 先验证是否过期。如果过期会抛出异常，全局捕获。之后的代码不会执行
            JwtUtils.verifyJwt(token, JwtUtils.DEFAULT_SECRET);

            // 如果没有抛出异常，表示token有效。则在Redis中寻找对应的登录信息
            String userId = JwtUtils.getClaimByKey(token, "userId");
            String redisToken = (String) redisUtils.get("user:" + userId);

            // 将来自客户端的token与redis中的token进行比较
            // 如果不一致，说明账号被其他人在其他地方登录了。拦截该请求，要求重新登录
            if (!ObjectUtils.isEmpty(redisToken) && !token.equals(redisToken)) {
                writeJson(ResultModel.failed(StatusCode.FORCED_OFFLINE));
                return false;
            }

            // 刷新token
            refreshToken(token, userId);

            // 如果最终来到这里，则放行请求
            // 在放行之前，将用户信息userId放到request，方便controller中取用
            request.setAttribute("userId", userId);

            return true;
        }
    }

    private void refreshToken(String token, String userId) {
        LocalDateTime expireAt = DateTimeUtils.toLocalDateTime(JwtUtils.getExpireAt(token));
        // 当前时间距离token过期还有多少天
        long days = DateTimeUtils.dif(LocalDateTime.now(), expireAt, ChronoUnit.DAYS);
        if (days < 1) { // 如果不足一天，则签发新的token。放到响应头中
            String newToken = userService.generateAndSaveToken(userId);
            response.setHeader("token", newToken);
        }
    }

    /**
     * 尝试从请求参数中获取token
     * @param request
     * @return          token字符串，获取不到返回null
     */
    private String tryGetToken(HttpServletRequest request) {
        // 从请求头中获取
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            // 从请求头中的token字段（自定义字段）尝试获取jwt token
            token = request.getHeader("token");
        }
        if (StringUtils.isEmpty(token)) {
            // 从url参数中尝试获取jwt token
            token = request.getParameter("token");
        }
        return token;
    }

    private void writeJson(ResultModel result) throws IOException {
        // 一定要设置。否则前端的ajax自动判断返回类型时会以为返回的是普通文本text
        response.setContentType("application/json; charset=utf-8");
        String jsonResult = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResult);
    }

}
