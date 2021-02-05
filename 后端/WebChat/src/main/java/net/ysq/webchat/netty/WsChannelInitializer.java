package net.ysq.webchat.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author passerbyYSQ
 * @create 2021-02-05 21:04
 */
public class WsChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // websocket基于http协议所需要的编码解码器
        pipeline.addLast(new HttpServerCodec())
                .addLast(new ChunkedWriteHandler())
                // 对httpMessage进行聚合处理，聚合成request或response
                .addLast(new HttpObjectAggregator(1024*64))
                // 处理握手和心跳
                .addLast(new WebSocketServerProtocolHandler("/ws"))
                // 自定义的业务的handler
                .addLast(new TextMsgHandler());
    }
}
