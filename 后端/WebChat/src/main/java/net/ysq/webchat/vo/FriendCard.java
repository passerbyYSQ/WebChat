package net.ysq.webchat.vo;

/**
 * 用于显示好友列表
 * @author passerbyYSQ
 * @create 2021-02-04 14:29
 */
public class FriendCard {

    private String userId; // 注意，这里是userId，而不是MyFriend表的主键id
    private String username;
    private String remarksName; // 备注名
    private String faceImg; // 小头像

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRemarksName() {
        return remarksName;
    }

    public void setRemarksName(String remarksName) {
        this.remarksName = remarksName;
    }

    public String getFaceImg() {
        return faceImg;
    }

    public void setFaceImg(String faceImg) {
        this.faceImg = faceImg;
    }
}
