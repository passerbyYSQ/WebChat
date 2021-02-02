package net.ysq.webchat.service.impl;

import com.google.zxing.WriterException;
import net.ysq.webchat.dao.UserMapper;
import net.ysq.webchat.po.User;
import net.ysq.webchat.service.UserService;
import net.ysq.webchat.utils.*;
import net.ysq.webchat.vo.LoginReqVo;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author passerbyYSQ
 * @create 2021-01-29 0:37
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private Sid sid;

    @Override
    public String generateAndUploadQRCode(String content, String photoUrl, String userId) {
        try {
            // 不带logo的二维码
            BufferedImage matrixImage = QRCodeUtils.generate(320, content);
            if (!ObjectUtils.isEmpty(photoUrl)) {
                // 在不带logo的二维码上绘制logo
                // matrixImage在内部被改变了
                QRCodeUtils.generateWithLogo(matrixImage, photoUrl);
            }

            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            ImageIO.write(matrixImage, "jpg", byteOutStream);
            // 上传至阿里云oss
            return OssUtils.uploadQRCode(byteOutStream.toByteArray(), userId);

        } catch (IOException | WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User uploadAndUpdateFaceImg(String base64, String userId) {
        // base64解码
        Base64Utils.Base64DataBean base64DataBean = Base64Utils.base64ToBytes(base64);
        // 将头像上传至阿里云oss
        User user = OssUtils.uploadFaceImg(base64DataBean.getBytes(), userId, base64DataBean.getSuffix());

        // 更新oss中的二维码
        String qrCodeUrl = generateAndUploadQRCode("content", user.getFaceImageBig(), userId);
        user.setQrcode(qrCodeUrl);

        // 根据userId更新数据库记录
        updateUserById(user);

        return user;
    }

    @Override
    public User getUserByPhone(String phone) {
        User cond = new User();
        cond.setPhone(phone);
        return userMapper.selectOne(cond);
    }

    @Override
    public User getUserByUsername(String username) {
        User cond = new User();
        cond.setUsername(username);
        return userMapper.selectOne(cond);
    }

    @Override
    public int updateUserById(User user) {
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public User register(LoginReqVo vo) {
        // 对密码进行md5加密
        String md5Pwd = DigestUtils.md5DigestAsHex(vo.getPassword().getBytes());
        String userId = sid.nextShort();

        // 二维码
        String qrCodeUrl = generateAndUploadQRCode("content", null, userId);

        User user = new User();
        user.setId(userId);
        user.setPhone(vo.getPhone());
        user.setUsername(userId); // 将userId作为初始化的username
        user.setPassword(md5Pwd);
        user.setCid(vo.getCid());
        user.setQrcode(qrCodeUrl);

        int cnt = userMapper.insertSelective(user);

        return cnt == 1 ? user : null;
    }

    @Override
    public String generateAndSaveToken(String userId) {
        // token的有效时长为7天
        long millis = TimeUnit.DAYS.toMillis(7);
        // 生成jwt
        String token = JwtUtils.generateJwt("userId", userId, JwtUtils.DEFAULT_SECRET, millis);
        // 存到redis中
        long seconds = TimeUnit.DAYS.toSeconds(7);
        redisUtils.set("user:"+ userId, token, seconds);
        return token;
    }


}
