package com.yang.springbootnetty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Yang
 * @date: 2019/7/23 23:08
 * @description:
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netty-websocket")
public class NettyWebsocketConfig {

    private int port;

}
