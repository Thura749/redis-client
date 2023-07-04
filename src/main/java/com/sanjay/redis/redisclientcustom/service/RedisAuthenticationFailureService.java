package com.sanjay.redis.redisclientcustom.service;

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
public class RedisAuthenticationFailureService {
    private static final String FAILURE_KEY = "record:{0}";
    private static final String LOCK_KEY = "lock:{0};";
    private final RedisTemplate<String, WrongUsernameOrPasswordDto> redisTemplate;
    private final RedisTemplate<String, Instant> lockRedisTemplate;

    public void saveUsernameOrPasswordRecord(String username) {
        var wrongUser = new WrongUsernameOrPasswordDto();
        wrongUser.setUsername(username);
        var savedUser = getWrongUser(username);
        if (savedUser.isEmpty()) {
            wrongUser = savedUser.get();
            int wrongTime = wrongUser.getWrongTimes() + 1;
            saveWrongUser(wrongUser);
        } else {
            wrongUser.setWrongTimes(1);
            wrongUser.setCreatedTime(Instant.now());
            saveWrongUser(wrongUser);
        }
        if (wrongUser.getWrongTimes() >= 5) {
            lock(wrongUser);
        }
    }

    public Instant getLockDuration(String username){
        return lockRedisTemplate.opsForValue().get(getLockedKey(username));
    }

    public Boolean isLock(String username){
        return lockRedisTemplate.hasKey(getLockedKey(username));
    }

    private Optional<WrongUsernameOrPasswordDto> getWrongUser(String username) {
        final var invalidGuest = redisTemplate.opsForValue().get(getFailureKey(username));
        if (Objects.nonNull(invalidGuest)) {
            return Optional.of(invalidGuest);
        }
        return Optional.empty();
    }

    private void saveWrongUser(WrongUsernameOrPasswordDto dto) {
        redisTemplate.opsForValue().set(getFailureKey(dto.getUsername()), dto, Duration.ofMinutes(5));
    }

    private String getFailureKey(String username) {
        return MessageFormat.format(FAILURE_KEY, username);
    }

    private void lock(WrongUsernameOrPasswordDto dto) {
        lockRedisTemplate.opsForValue().set(getLockedKey(dto.getUsername()), Instant.now(), Duration.ofMinutes(5));
    }

    private String getLockedKey(String username){
        return MessageFormat.format(LOCK_KEY, username);
    }
}
