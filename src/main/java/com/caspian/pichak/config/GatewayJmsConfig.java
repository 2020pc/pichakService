package com.caspian.pichak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("jms")
@ImportResource("classpath:applicationContext-gateway-spi.xml")
public class GatewayJmsConfig {
}
