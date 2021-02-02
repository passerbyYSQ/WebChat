package net.ysq.webchat.vo;

import net.ysq.webchat.po.User;
import org.springframework.beans.BeanUtils;

/**
 * 搜索用户返回的用户信息
 * 就是User的精简版
 *
 * @author passerbyYSQ
 * @create 2021-02-02 13:39
 */
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Byte getSex() {
        return sex;
    }

    public void setSex(Byte sex) {
        this.sex = sex;
    }

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
