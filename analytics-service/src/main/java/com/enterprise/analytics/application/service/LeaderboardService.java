package com.enterprise.analytics.application.service;

import com.enterprise.analytics.application.dto.LeaderboardEntryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final StringRedisTemplate redisTemplate;

    public void updateScore(String type, String period, String memberId, double score) {
        String key = "leaderboard:" + type.toLowerCase() + ":" + period;
        log.info("Updating leaderboard [{}] for member [{}] with score [{}]", key, memberId, score);
        redisTemplate.opsForZSet().add(key, memberId, score);
    }

    public List<LeaderboardEntryDTO> getTopN(String type, String period, int n) {
        String key = "leaderboard:" + type.toLowerCase() + ":" + period;
        log.info("Fetching top {} from leaderboard [{}]", n, key);

        Set<ZSetOperations.TypedTuple<String>> topEntries = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);

        List<LeaderboardEntryDTO> result = new ArrayList<>();
        if (topEntries == null) return result;

        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : topEntries) {
            result.add(LeaderboardEntryDTO.builder()
                    .entityId(tuple.getValue())
                    .score(tuple.getScore() != null ? tuple.getScore() : 0.0)
                    .rank(rank++)
                    .build());
        }

        return result;
    }
    
    public void reset(String type, String period) {
        String key = "leaderboard:" + type.toLowerCase() + ":" + period;
        redisTemplate.delete(key);
    }
}
