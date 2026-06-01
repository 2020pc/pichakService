package com.caspian.pichak.config;

import com.caspian.moderngateway.spi.service.ChannelManagerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Proxy;

@Configuration
@Profile("!jms")
public class GatewayDisabledConfig {

    @Bean
    public ChannelManagerProvider channelManagerProvider() {
        return (ChannelManagerProvider) Proxy.newProxyInstance(
                ChannelManagerProvider.class.getClassLoader(),
                new Class<?>[]{ChannelManagerProvider.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();

                    if ("toString".equals(methodName)) {
                        return "Disabled ChannelManagerProvider";
                    }

                    if ("hashCode".equals(methodName)) {
                        return System.identityHashCode(proxy);
                    }

                    if ("equals".equals(methodName)) {
                        return proxy == args[0];
                    }

                    throw new IllegalStateException(
                            "JMS/WebLogic is disabled. Run the app with profile 'jms' to use the real ChannelManagerProvider."
                    );
                }
        );
    }
}