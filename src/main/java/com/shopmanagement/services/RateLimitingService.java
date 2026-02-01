package com.shopmanagement.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // 10 requests per minute for unauthenticated IPs
    public Bucket resolveBucket(String key, boolean isAuthenticated) {
        return cache.computeIfAbsent(key, k -> newBucket(isAuthenticated));
    }

    private Bucket newBucket(boolean isAuthenticated) {
        Bandwidth limit;
        if (isAuthenticated) {
            // Authenticated users: 50 requests per minute
             limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofMinutes(1)));
        } else {
            // Unauthenticated IPs: 10 requests per minute
             limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        }
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
