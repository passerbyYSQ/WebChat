package net.ysq.webchat.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author passerbyYSQ
 * @create 2021-01-31 2:35
 */
public class CloseUtils {

    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable == null) {
                return;
            }
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
