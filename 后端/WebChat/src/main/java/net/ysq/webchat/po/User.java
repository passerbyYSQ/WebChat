package net.ysq.webchat.po;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "user")
public class User {

    // 注意包不要导入错误了：javax.persistence.Transient;
    @Transient
    private String token;

    @Id
    private String id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 0：男；1：女；2：保密
     */
    private Byte sex;

    /**
     * 用户名，账号，慕信号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 我的头像，如果没有默认给一张
     */
    @Column(name = "face_image")
    private String faceImage;

    @Column(name = "face_image_big")
    private String faceImageBig;

    /**
     * 个性签名
     */
    private String description;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 新用户注册后默认后台生成二维码，并且上传到fastdfs
     */
    private String qrcode;

    /**
     * 设备标识码
     */
    private String cid;

}
