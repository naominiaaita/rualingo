package com.example.rualingo.service;

import com.example.rualingo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<LeaderboardEntry> getTopUsers(int limit) {
        return userRepository.findTop10ByOrderByStreakDesc().stream()
                .filter(Objects::nonNull)
                .map(user -> new LeaderboardEntry(user.getUsername(), (double) (user.getStreak() != null ? user.getStreak() : 0)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static record LeaderboardEntry(String username, Double score) {}
}
