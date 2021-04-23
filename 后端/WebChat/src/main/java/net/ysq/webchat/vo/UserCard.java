package net.ysq.webchat.vo;

import lombok.Data;
import net.ysq.webchat.po.User;
import org.springframework.beans.BeanUtils;

/**
 * 搜索用户返回的用户信息
 * 就是User的精简版
 *
 * @author passerbyYSQ
 * @create 2021-02-02 13:39
 */
@Data
public class UserCard {

    private String id;
    private String username;
    private Byte sex;
    private String faceImage;
    private String description;

    public UserCard() {
    }

    public UserCard(User user) {
        BeanUtils.copyProperties(user, this);
    }

}
