package com.sanjay.redis.redisclientcustom.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class WrongUsernameOrPasswordDto {
    private String username;
    private int wrongTimes;
    private Instant createdTime;
}
