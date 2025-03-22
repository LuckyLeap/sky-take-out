package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springdoc.core.models.GroupedOpenApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/employee/login",
                        "/v3/api-docs/**",       // 排除 SpringDoc 的 OpenAPI JSON 路径
                        "/doc.html",             // 排除 Knife4j 的文档页面
                        "/webjars/**",           // 排除 Knife4j 的静态资源
                        "/swagger-ui/**"         // 排除 Swagger UI 路径
                );
    }

    /**
     * 配置 OpenAPI 文档信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("苍穹外卖项目接口文档")          // 文档标题
                        .contact(new Contact().name("鸆灏")) //作者
                        .version("2.0")                     // 文档版本
                        .description("苍穹外卖项目接口文档"));  // 文档描述
    }

    /**
     * 配置控制器扫描范围
     */
    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("all")                   // 分组名称
                .packagesToScan("com.sky.controller")  // 扫描控制器包
                .build();
    }

    /**
     * 设置静态资源映射（Knife4j 的资源路径）
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Knife4j 的文档页面和资源
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展MVC消息转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展MVC消息转换器...");
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 Java 8 日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置反序列化格式：前端输入的日期格式（yyyyMMddHHmmss）
        DateTimeFormatter deserializationFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(deserializationFormatter));

        // 配置序列化格式：后端返回的日期格式（yyyy-MM-dd HH:mm）
        DateTimeFormatter serializationFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(serializationFormatter));

        objectMapper.registerModule(javaTimeModule);
        // 禁用时间戳格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 将配置后的 ObjectMapper 设置到转换器
        converter.setObjectMapper(objectMapper);
        converters.add(1, converter);
   }
}