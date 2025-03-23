package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 */
@Slf4j
@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        try {
            // 记录开始创建Redis模板对象的日志
            log.info("开始创建Redis模板对象...");

            // 初始化RedisTemplate
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

            // 设置Redis连接工厂
            redisTemplate.setConnectionFactory(redisConnectionFactory);

            // 配置序列化器
            configureSerializers(redisTemplate);

            // 记录Redis模板对象创建完成的日志
            log.info("Redis模板对象创建完成");
            return redisTemplate;
        } catch (Exception e) {
            // 捕获异常并记录详细错误信息
            log.error("Redis模板对象创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("Redis模板对象初始化失败", e);
        }
    }

    /**
     * 配置RedisTemplate的序列化器
     */
    private void configureSerializers(RedisTemplate<String, Object> redisTemplate) {
        // 设置Key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置HashKey的序列化器
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    }
}