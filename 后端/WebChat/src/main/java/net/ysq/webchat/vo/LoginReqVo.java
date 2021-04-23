package net.ysq.webchat.vo;

import lombok.Data;
import net.ysq.webchat.common.Constant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 登录请求的vo
 *
 * @author passerbyYSQ
 * @create 2021-01-29 1:19
 */
@Data
public class LoginReqVo {

    @NotBlank
    @Length(min = 3, max = 32)
    @Pattern(regexp = Constant.REGEX_PHONE)
    private String phone;

    @NotBlank
    @Length(min = 6)
    private String password;

    // 设备唯一标识
//    @NotBlank
    private String cid;

}
