package net.ysq.webchat.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author passerbyYSQ
 * @create 2021-01-31 1:34
 */
public class Base64Utils {

    // 匹配常见图片的base64编码的正则表达式
    public static final String REGEX =  "^(data:image/(jpg|jpeg|png);base64,)(\\S+)";
    public static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * 将base64编码转成文件保存
     * @param base64Data    完整的base64编码
     * @param destDir       目标目录
     * @return
     */
    public static boolean base64ToFile(String base64Data, File destDir, String fileName) {
        Matcher matcher = pattern.matcher(base64Data);
        if (!matcher.find()) {
            return false;
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 取出分组信息，必须要在matcher.find()==true之后
        String suffix = matcher.group(2); // 取出图片的后缀
        String data = matcher.group(3); // 取出数据部分

        File tmpFile = new File(destDir, fileName + "." + suffix);
        byte[] bytes = Base64.getDecoder().decode(data);

        // 流的拷贝，可以使用原始的bio方式。在这里使用nio的通道相关的API来操作
        ReadableByteChannel inChannel = Channels.newChannel(new ByteArrayInputStream(bytes));
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(tmpFile, true).getChannel();
            outChannel.transferFrom(inChannel, outChannel.size(), bytes.length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtils.close(inChannel, outChannel);
        }
    }

    /**
     * 将bas364编码转成byte[]
     * @param base64Data
     * @return
     */
    public static Base64DataBean base64ToBytes(String base64Data) {
        Matcher matcher = pattern.matcher(base64Data);
        if (!matcher.find()) {
            return null;
        }
        String suffix = matcher.group(2); // 取出图片的后缀
        String data = matcher.group(3); // 取出data部分
        byte[] bytes = Base64.getDecoder().decode(data);

        return new Base64DataBean(suffix, bytes);
    }

    static public class Base64DataBean {
        String suffix;
        byte[] bytes;

        public Base64DataBean(String suffix, byte[] bytes) {
            this.suffix = suffix;
            this.bytes = bytes;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }
}
