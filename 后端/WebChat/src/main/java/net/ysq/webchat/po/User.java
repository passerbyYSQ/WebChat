package net.ysq.webchat.po;

import javax.persistence.*;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取手机号
     *
     * @return phone - 手机号
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号
     *
     * @param phone 手机号
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取0：男；1：女；2：保密
     *
     * @return sex - 0：男；1：女；2：保密
     */
    public Byte getSex() {
        return sex;
    }

    /**
     * 设置0：男；1：女；2：保密
     *
     * @param sex 0：男；1：女；2：保密
     */
    public void setSex(Byte sex) {
        this.sex = sex;
    }

    /**
     * 获取用户名，账号，慕信号
     *
     * @return username - 用户名，账号，慕信号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名，账号，慕信号
     *
     * @param username 用户名，账号，慕信号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取我的头像，如果没有默认给一张
     *
     * @return face_image - 我的头像，如果没有默认给一张
     */
    public String getFaceImage() {
        return faceImage;
    }

    /**
     * 设置我的头像，如果没有默认给一张
     *
     * @param faceImage 我的头像，如果没有默认给一张
     */
    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    /**
     * @return face_image_big
     */
    public String getFaceImageBig() {
        return faceImageBig;
    }

    /**
     * @param faceImageBig
     */
    public void setFaceImageBig(String faceImageBig) {
        this.faceImageBig = faceImageBig;
    }

    /**
     * 获取个性签名
     *
     * @return description - 个性签名
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置个性签名
     *
     * @param description 个性签名
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取昵称
     *
     * @return nickname - 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置昵称
     *
     * @param nickname 昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取新用户注册后默认后台生成二维码，并且上传到fastdfs
     *
     * @return qrcode - 新用户注册后默认后台生成二维码，并且上传到fastdfs
     */
    public String getQrcode() {
        return qrcode;
    }

    /**
     * 设置新用户注册后默认后台生成二维码，并且上传到fastdfs
     *
     * @param qrcode 新用户注册后默认后台生成二维码，并且上传到fastdfs
     */
    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    /**
     * 获取设备标识码
     *
     * @return cid - 设备标识码
     */
    public String getCid() {
        return cid;
    }

    /**
     * 设置设备标识码
     *
     * @param cid 设备标识码
     */
    public void setCid(String cid) {
        this.cid = cid;
    }
}
