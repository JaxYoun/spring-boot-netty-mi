package com.yang.springbootnetty.config;

import com.yang.springbootnetty.netty.WebSocketServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: Yang
 * @date: 2019/7/23 22:34
 * @description:
 */
@Component
public class NettyWebsocketBooter implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private NettyWebsocketConfig nettyWebsocketConfig;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            WebSocketServer.getInstance().start(this.nettyWebsocketConfig.getPort());
        }
    }

}
