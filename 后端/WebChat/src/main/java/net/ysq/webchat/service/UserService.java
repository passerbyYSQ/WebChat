package net.ysq.webchat.service;

import net.ysq.webchat.po.User;
import net.ysq.webchat.vo.LoginReqVo;

import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2021-01-29 0:36
 */
public interface UserService {

    /**
     * 生成个人二维码并上传至阿里云oss
     * @param content   写入到二维码中的内容
     * @param photoUrl  用户头像的公网url
     * @return          二维码的公网url
     */
    String generateAndUploadQRCode(String content, String photoUrl, String userId);

    /**
     * 将头像上传至阿里云oss，并修改数据库记录
     */
    User uploadAndUpdateFaceImg(String base64, String userId);

    /**
     * 根据手机号查找用户
     */
    User getUserByPhone(String phone);

    /**
     * 根据id查找用户
     */
    User getUserById(String userId);

    /**
     * 根据用户名进行模糊匹配
     * @return
     */
    List<User> getUserLikeUsername(String username);

    /**
     * 根据用户名精确查找用户
     */
    User getUserByUsername(String username);

    /**
     * 修改user中不为空的字段
     */
    int updateUserById(User user);

    /**
     * 新加一条用户记录
     */
    User register(LoginReqVo vo);

    /**
     * 生成登录状态的token
     */
    String generateAndSaveToken(String userId);

}
