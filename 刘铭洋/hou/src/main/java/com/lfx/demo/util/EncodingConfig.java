package com.lfx.demo.util;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全面的编码配置类
 * 解决中文字符编码问题
 */
@Configuration
public class EncodingConfig implements WebMvcConfigurer {

    /**
     * 配置字符串消息转换器使用UTF-8编码
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(0, stringConverter);
    }

    /**
     * 配置内容协商
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .favorPathExtension(false)
                .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    }

    /**
     * 自定义Tomcat连接器配置
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
            connector.setURIEncoding(StandardCharsets.UTF_8.name());
            connector.setUseBodyEncodingForURI(true);
            
            // 设置更宽松的字符编码范围，支持中文字符
            connector.setProperty("relaxedPathChars", "[]|{}^`\"<>");
            connector.setProperty("relaxedQueryChars", "[]|{}^`\"<>");
            
            // 确保中文字符能正确处理
            connector.setProperty("maxParameterCount", "10000");
            connector.setProperty("maxPostSize", String.valueOf(50 * 1024 * 1024)); // 50MB
            
            // 添加对Unicode字符的支持
            connector.setProperty("allowedRequestAttributesPattern", ".*");
        });
    }
} 