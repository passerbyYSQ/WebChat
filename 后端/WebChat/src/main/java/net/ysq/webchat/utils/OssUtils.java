package net.ysq.webchat.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import net.ysq.webchat.po.User;

import java.io.ByteArrayInputStream;

/**
 * @author passerbyYSQ
 * @create 2021-01-31 14:19
 */
public class OssUtils {

    private static final String END_POINT = "https://oss-cn-shenzhen.aliyuncs.com";
    // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高
    // 强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录RAM控制台创建RAM账号。
    private static final String ACCESS_KEY_ID = "LTAI4GFvPgPakyd9rT7ahbXf";
    private static final String ACCESS_KEY_SECRET = "jd3QMuwTmRxZCP77NCnKW5Q2arWfUo";
    private static final String BUCKET_NAME = "webchat-ysq";
    private static final String DOMAIN_NAME = "https://webchat-ysq.oss-cn-shenzhen.aliyuncs.com/";

    /**
     *
     * @param bytes
     * @param objectName    上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     */
    public static void upload(byte[] bytes, String objectName) {
        OSS ossClient = new OSSClientBuilder().build(END_POINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        ossClient.putObject(BUCKET_NAME, objectName, new ByteArrayInputStream(bytes));
        ossClient.shutdown();
    }

    /**
     * 域名/sample.jpg?x-oss-process=style/stylename
     * 上传头像
     * @param bytes
     * @param suffix    图片后缀
     * @return          user中的头像url已经被设置进去了
     */
    public static User uploadFaceImg(byte[] bytes, String userId, String suffix) {
        String objectName = String.format("user/%s/face_img.%s", userId, suffix);
        upload(bytes, objectName);

        // 拼接url。仅限于权限为公共读
        String original = DOMAIN_NAME + objectName; // 原图
        String thumb = original + "?x-oss-process=style/face_img"; // 缩略图

        String timestamp = "timestamp=" + System.currentTimeMillis();
        User user = new User();
        user.setId(userId);
        user.setFaceImageBig(original + "?" + timestamp);
        user.setFaceImage(thumb + "&" + timestamp);

        return user;
    }

    public static String uploadQRCode(byte[] bytes, String userId) {
        String objectName = String.format("user/%s/qrcode.jpg", userId);
        upload(bytes, objectName);

        // 拼接url。仅限于权限为公共读
        return (DOMAIN_NAME + objectName);
    }


}
