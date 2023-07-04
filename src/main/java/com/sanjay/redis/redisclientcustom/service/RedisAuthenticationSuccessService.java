package com.sanjay.redis.redisclientcustom.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanjay.redis.redisclientcustom.dto.WrongUsernameOrPasswordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisAuthenticationSuccessService<T> {
    private static final String AUTH_KEY = "auth:{0}";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper;

    public void saveAuth(String key, T t, long duration) {
        try {
            redisTemplate.opsForValue().set(keyFormat(key), mapper.writeValueAsString(t),
                    Duration.ofMinutes(duration));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSuccess(String key) {
        return redisTemplate.hasKey(keyFormat(key));
    }

    public Optional<T> getAuth(String key) {
        final var response = redisTemplate.opsForValue().get(keyFormat(key));
        Optional<T> result = Optional.empty();
        if(Objects.nonNull(response)) {
            try {
                result = Optional.of(mapper.readValue(response, new TypeReference<T>() {
                }));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private String keyFormat(String key) {
        return MessageFormat.format(AUTH_KEY, key);
    }
}
