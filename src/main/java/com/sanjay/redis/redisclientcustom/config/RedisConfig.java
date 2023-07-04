package com.sanjay.redis.redisclientcustom.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sanjay.redis.redisclientcustom.dto.WrongUsernameOrPasswordDto;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Instant;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Setter
public class RedisConfig {
    private String host;
    private String password;

    @Bean
    public JedisConnectionFactory jedisPoolConfig(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(200);
        config.setMaxTotal(300);
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.setHostName(host);
        factory.setPassword(password);
        factory.setPort(6379);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, WrongUsernameOrPasswordDto> wrongUserRedisTemplate(
            JedisConnectionFactory jedisConnectionFactory) {
        return buildTemplate(jedisConnectionFactory, WrongUsernameOrPasswordDto.class);
    }

    @Bean
    public RedisTemplate<String, String> StringRedisTemplate(
            JedisConnectionFactory jedisConnectionFactory) {
        return buildTemplate(jedisConnectionFactory, String.class);
    }

    @Bean
    public RedisTemplate<String, Instant> lockDurationRedisTemplate(
            JedisConnectionFactory jedisConnectionFactory) {
        return buildTemplate(jedisConnectionFactory, Instant.class);
    }

    public <T> RedisTemplate<String, T> buildTemplate(
            JedisConnectionFactory jedisConnectionFactory,
            Class<T> t) {
        final RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        final var jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(t);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper());
        final var stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    private ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
