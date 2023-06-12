package dev.saha.redis_test.controller;

import dev.saha.redis_test.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RetrieveController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;


    @PostMapping
    public ResponseEntity<?> getDataFromRedis(){
        return ResponseEntity.of(Optional.ofNullable(redisService.getAllAccountsFromRedis()));
    }

    @PostMapping("get-one")
    public ResponseEntity<?> getSingleDataFromRedis(){
        return ResponseEntity.of(Optional.ofNullable(redisService.getData()));
    }


    @DeleteMapping("/delete-all")
    public void deleteAllRedisData() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

}
