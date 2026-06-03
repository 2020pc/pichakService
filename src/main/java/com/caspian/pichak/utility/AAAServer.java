package com.caspian.pichak.utility;

import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Service;

@Service
public class AAAServer {
    public static final Logger logger = LogManager.getLogger(AAAServer.class);
//    public static final List<Integer> num = Arrays.asList(1, 2, 3, 4, 5);
//    @Autowired
//    private Environment environment;

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }

//    @Bean
//    public JndiTemplate jndiTemplate() {
//        JndiTemplate template = new JndiTemplate();
//        Properties properties = new Properties();
//        properties.setProperty("java.naming.factory.initial", "weblogic.jndi.WLInitialContextFactory");
//        properties.setProperty("java.naming.provider.url", this.environment.getProperty("lotus.weblogic.url"));
//        template.setEnvironment(properties);
//        return template;
//    }
}
