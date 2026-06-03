package com.caspian.pichak.config;

import com.caspian.moderngateway.spi.service.JmsChannelManagerProvider;
import com.caspian.moderngateway.spi.service.JmsService;
import com.caspian.moderngateway.spi.util.ResponseFilterGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.NamingException;
import java.util.Properties;

@Configuration
public class GatewayJmsConfig {

    // ==================== JNDI Template ====================
    @Bean
    public JndiTemplate jndiTemplate(
            @Value("${gateway.spi.jms.factory.initial}") String factoryInitial,
            @Value("${gateway.spi.jms.provider.url}") String providerUrl) {
        JndiTemplate template = new JndiTemplate();
        Properties env = new Properties();
        env.put("java.naming.factory.initial", factoryInitial);
        env.put("java.naming.provider.url", providerUrl);
        env.put("weblogic.jndi.connectTimeout", "1000");
        env.put("weblogic.jndi.responseReadTimeout", "1000");
        template.setEnvironment(env);
        return template;
    }

    // ==================== Connection Factory ====================
    @Bean
    public ConnectionFactory connectionFactory(JndiTemplate jndiTemplate,
                                               @Value("${gateway.spi.jms.connectionFactory}") String cfName) throws NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiTemplate(jndiTemplate);
        bean.setJndiName(cfName);
        bean.setExpectedType(ConnectionFactory.class);
        bean.afterPropertiesSet();  // <-- IMPORTANT: Initialize first
        return (ConnectionFactory) bean.getObject();
    }

    // ==================== Caching Connection Factory ====================
    @Bean
    public CachingConnectionFactory cachingConnectionFactory(ConnectionFactory connectionFactory) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setTargetConnectionFactory(connectionFactory);
        factory.setSessionCacheSize(10);
        return factory;
    }

    // ==================== Request Queue ====================
    @Bean
    public Queue requestQueue(JndiTemplate jndiTemplate,
                              @Value("${gateway.spi.jms.requestQueue}") String queueName) throws NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiTemplate(jndiTemplate);
        bean.setJndiName(queueName);
        bean.setExpectedType(Queue.class);
        bean.afterPropertiesSet();
        return (Queue) bean.getObject();
    }

    // ==================== Response Queue ====================
    @Bean
    public Queue responseQueue(JndiTemplate jndiTemplate,
                               @Value("${gateway.spi.jms.responseQueue}") String queueName) throws NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiTemplate(jndiTemplate);
        bean.setJndiName(queueName);
        bean.setExpectedType(Queue.class);
        bean.afterPropertiesSet();  // <-- IMPORTANT
        return (Queue) bean.getObject();
    }

    // ==================== JMS Template ====================
    @Bean
    public JmsTemplate jmsTemplate(CachingConnectionFactory cachingConnectionFactory,
                                   @Value("${gateway.spi.jms.delivery.mode}") int deliveryMode,
                                   @Value("${gateway.spi.jms.message.ttl}") long ttl,
                                   Queue requestQueue) {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(cachingConnectionFactory);
        template.setDefaultDestination(requestQueue);
        template.setSessionAcknowledgeMode(1);
        template.setDeliveryMode(deliveryMode);
        template.setExplicitQosEnabled(true);
        template.setTimeToLive(ttl);
        return template;
    }

    // ==================== JMS Service ====================
    @Bean
    public JmsService jmsService(JmsTemplate jmsTemplate,
                                 @Value("${gateway.spi.jms.message.selector}") String selector,
                                 @Value("${gateway.spi.jms.channel.name}") String channelName,
                                 @Value("${gateway.spi.jms.delivery.mode}") int deliveryMode,
                                 @Value("${gateway.spi.jms.message.ttl}") long ttl,
                                 @Value("${gateway.spi.jms.channel.format}") String format,
                                 @Value("${gateway.spi.jms.provider.url}") String providerUrl) {
        JmsService service = new JmsService();
        service.setJmsTemplate(jmsTemplate);
        service.setChannelManagerMessageSelector(selector);
        service.setChannelName(channelName);
        service.setMessageDeliveryMode(deliveryMode);
        service.setMessageTimeout(ttl);
        service.setChannelFormat(format);
        service.setJmsProviderUrl(providerUrl);
        service.setResponseFilter(ResponseFilterGenerator.getUUIDFilter());
        return service;
    }

    // ==================== Channel Manager Provider ====================
    @Bean
    public JmsChannelManagerProvider channelManagerProvider(JmsService jmsService) {
        JmsChannelManagerProvider provider = new JmsChannelManagerProvider();
        provider.setFilter(ResponseFilterGenerator.getUUIDFilter());
        provider.setSender(jmsService);
        return provider;
    }
}