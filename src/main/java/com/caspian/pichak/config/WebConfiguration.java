package com.caspian.pichak.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(System.getProperty("user.home"));
        String homeDirectory = path.toUri().toString();
        registry.addResourceHandler(new String[]{"/**"});
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return cookieLocaleResolver;
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public Gson gson() {

        return new Gson();
    }
}
