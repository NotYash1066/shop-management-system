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
            // Authenticated users: 500 requests per second for stress testing
             limit = Bandwidth.classic(500, Refill.greedy(500, Duration.ofSeconds(1)));
        } else {
            // Unauthenticated IPs: 100 requests per second
             limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofSeconds(1)));
        }
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
