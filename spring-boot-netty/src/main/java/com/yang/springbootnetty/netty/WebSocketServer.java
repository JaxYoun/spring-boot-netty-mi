package com.yang.springbootnetty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author: Yang
 * @date: 2019/7/23 22:17
 * @description: 单例的WebSocketServer类
 */
@Slf4j
public class WebSocketServer {

    private static final EventLoopGroup PARENT_GROUP = new NioEventLoopGroup();
    private static final EventLoopGroup CHILD_GROUP = new NioEventLoopGroup();

    private WebSocketServer() {
    }

    private static class Holder {
        private static final WebSocketServer instance = new WebSocketServer();
    }

    public static WebSocketServer getInstance() {
        return Holder.instance;
    }

    /**
     * 启动WebSocketServer
     *
     * @param port 监听端口
     */
    public void start(int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(PARENT_GROUP, CHILD_GROUP)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebsocketChanneInitializer());
        serverBootstrap.bind(port);
        log.info("Netty websocket server listening on {}", port);
    }

    /**
     * Websocket管道初始化器
     */
    private static class WebsocketChanneInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
            pipeline.addLast(new WebsocketHandler());
        }
    }

    /**
     * Websocket消息处理器
     */
    @Slf4j
    private static class WebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

        private static final ChannelGroup CLIENT_CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
            //只给当前客户端返回数据
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(textWebSocketFrame.text() + LocalDateTime.now()));
            //遍及集合中的所有通道，群发
            CLIENT_CHANNEL_GROUP.writeAndFlush(new TextWebSocketFrame(textWebSocketFrame.text() + LocalDateTime.now()));

        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            CLIENT_CHANNEL_GROUP.add(ctx.channel());
            log.info("当前在线用户数：{}", CLIENT_CHANNEL_GROUP.size());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            log.info("当前在线用户数：{}", CLIENT_CHANNEL_GROUP.size());
        }
    }
}
