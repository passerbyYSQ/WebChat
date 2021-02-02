package net.ysq.webchat.vo;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 登录请求的vo
 *
 * @author passerbyYSQ
 * @create 2021-01-29 1:19
 */
public class LoginReqVo {

    @NotBlank
    @Length(min = 3, max = 32)
    @Pattern(regexp = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$")
    private String phone;

    @NotBlank
    @Length(min = 6)
    private String password;

    // 设备唯一标识
//    @NotBlank
    private String cid;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

}
