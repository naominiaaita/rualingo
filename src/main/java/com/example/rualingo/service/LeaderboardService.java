package com.example.rualingo.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private static final String LEADERBOARD_KEY = "rualingo:leaderboard";
    private final RedisTemplate<String, String> redisTemplate;

    public LeaderboardService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateScore(String username, double score) {
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, username, score);
    }

    public Set<LeaderboardEntry> getTopUsers(int limit) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = 
            redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, limit - 1);

        if (typedTuples == null) return Set.of();

        return typedTuples.stream()
            .map(tuple -> new LeaderboardEntry(tuple.getValue(), tuple.getScore()))
            .collect(Collectors.toSet());
    }

    public static record LeaderboardEntry(String username, Double score) {}
}
