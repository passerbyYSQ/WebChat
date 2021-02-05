package net.ysq.webchat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

/**
 * netty和springboot的整合启动
 * Spring在创建bean的时候会调用WebSocketServer的空参构造，装载进IOC容器
 *
 * @author passerbyYSQ
 * @create 2021-02-05 20:59
 */
@Component  // 注意不要忘了！！！
public class WebSocketServer {

    private static class SingletonWSServer {
        static final WebSocketServer instance = new WebSocketServer();
    }

    // 获取单例的方法
    public static WebSocketServer getInstance() {
        return SingletonWSServer.instance;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    public WebSocketServer() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WsChannelInitializer());
    }

    public void start() {
        this.future = server.bind(8081);
        if (future.isSuccess()) {
            System.out.println("启动 Netty 成功");
        }
    }
}

