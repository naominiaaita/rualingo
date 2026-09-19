package com.example.rualingo.service;

import com.example.rualingo.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;

@Service
public class LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);
    private static final String LEADERBOARD_KEY = "rualingo:leaderboard";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    public LeaderboardService(RedisTemplate<String, String> redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    public void updateScore(String username, double score) {
        try {
            // Update Redis for high-performance ranking
            redisTemplate.opsForZSet().add(LEADERBOARD_KEY, username, score);
        } catch (Exception e) {
            // If Redis fails, we don't need to do anything here because 
            // the score (streak) is already persisted in the main MySQL/Postgres database
            log.warn("Redis unreachable: Score for {} not cached in Redis, falling back to SQL for reads.", username);
        }
    }

    public Set<LeaderboardEntry> getTopUsers(int limit) {
        try {
            // 1. Try fetching from Redis (Primary - Ultra Fast)
            Set<ZSetOperations.TypedTuple<String>> typedTuples = 
                redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, limit - 1);

            if (typedTuples != null && !typedTuples.isEmpty()) {
                return typedTuples.stream()
                    .filter(Objects::nonNull)
                    .map(tuple -> new LeaderboardEntry(tuple.getValue(), tuple.getScore()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        } catch (Exception e) {
            log.error("Redis unreachable: Falling back to SQL Database for Leaderboard.");
        }

        // 2. Fallback: Query the main SQL Database (Permanent Reliability)
        // This ensures the leaderboard ALWAYS works even if Redis is completely missing.
        return userRepository.findTop10ByOrderByStreakDesc().stream()
                .filter(Objects::nonNull)
                .map(user -> new LeaderboardEntry(user.getUsername(), (double) (user.getStreak() != null ? user.getStreak() : 0)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static record LeaderboardEntry(String username, Double score) {}
}
