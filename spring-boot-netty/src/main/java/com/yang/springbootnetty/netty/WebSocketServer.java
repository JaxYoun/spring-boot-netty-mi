package com.yang.springbootnetty.netty;

import com.yang.springbootnetty.common.JsonUtil;
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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: Yang
 * @date: 2019/7/23 22:17
 * @description: 单例的WebSocketServer类
 */
@Slf4j
public final class WebSocketServer {

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
            pipeline.addLast(new WebSocketServerProtocolHandler("/ws/{id}"));
            pipeline.addLast(new WebsocketHandler());
        }
    }

    /**
     * Websocket消息处理器
     */
    @Slf4j
    private static class WebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

        private static final ChannelGroup CLIENT_CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        private static final ConcurrentMap<Long, ChannelId> USER_ID_CHANNEL_ID_MAP = new ConcurrentHashMap<>(2048);

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
            /*//只给当前客户端返回数据
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(textWebSocketFrame.text() + LocalDateTime.now()));
            //遍及集合中的所有通道，群发
            CLIENT_CHANNEL_GROUP.writeAndFlush(new TextWebSocketFrame(textWebSocketFrame.text() + LocalDateTime.now()));*/


            /**
             * 1.获取消息体
             * 2.判断消息类型，区分对待：
             * 2.1连接建立：webSocket连接建立时，将用户id与channel做关联，并存入集合
             * 2.2消息发送：服务端收到用户消息后，将消息存入数据库，并做好离线标记
             * 2.3消息确认：消息签收确认
             * 2.4心跳消息：用以保持连接
             */
            String json = textWebSocketFrame.text();
            DataContent dataContent = JsonUtil.jsonToBean(json, DataContent.class);
            switch (dataContent.action) {
                case CONNECT: {
                    break;
                }
                case HERT_BEAT: {
                    break;
                }
                case CHAT: {
                    break;
                }
                case SIGNED: {
                    break;
                }
                default: {
                    break;
                }
            }
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            USER_ID_CHANNEL_ID_MAP.putIfAbsent(9L, channel.id());
            CLIENT_CHANNEL_GROUP.add(channel);
            log.info("上线--当前在线用户数：{}", CLIENT_CHANNEL_GROUP.size());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            CLIENT_CHANNEL_GROUP.remove(ctx.channel());
            log.info("下线--当前在线用户数：{}", CLIENT_CHANNEL_GROUP.size());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.channel().close();
            CLIENT_CHANNEL_GROUP.remove(ctx.channel());
            log.info(cause.getMessage(), cause);
        }
    }

    /**
     * 消息内容类
     */
    @Data
    private static class DataContent implements Serializable {

        /**
         * 动作类型
         */
        private ActionEnum action;

        /**
         * 聊天消息体
         */
        private ChatMsg chatMsg;

        /**
         * 额外保留字段
         */
        private String extend;
    }

    /**
     * 聊天消息
     */
    @Data
    private static class ChatMsg {

    }

    /**
     * 消息类型枚举
     */
    private enum ActionEnum {

        /**
         * 连接
         */
        CONNECT,

        /**
         * 聊天
         */
        CHAT,

        /**
         * 签收
         */
        SIGNED,

        /**
         * 心跳
         */
        HERT_BEAT
    }
}
