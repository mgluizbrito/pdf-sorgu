package io.github.mgluizbrito.PdfSorgu.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.mgluizbrito.PdfSorgu.dto.UserStatusResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketManagerService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private final Bandwidth anonUploadLimit = Bandwidth.builder()
            .capacity(2)
            .refillGreedy(2, Duration.ofDays(1))
            .build();

    private final Bandwidth anonQueryLimit = Bandwidth.builder()
            .capacity(6)
            .refillGreedy(6, Duration.ofDays(1))
            .build();

    private final Bandwidth authUploadLimit = Bandwidth.builder()
            .capacity(5)
            .refillGreedy(5, Duration.ofDays(1))
            .build();

    private final Bandwidth authQueryLimit = Bandwidth.builder()
            .capacity(20)
            .refillGreedy(20, Duration.ofDays(1))
            .build();

    /**
     * Returns the Bucket (bucket of tokens) for an identifier and type of operation.
     * @param identifier The unique ID (username or IP)
     * @param isUpload Whether the operation is an upload (true) or query (false)
     * @param isAuthenticated If the user is logged in
     * @return The Bucket configured for the limit.
     */
    public Bucket resolveBucket(String identifier, boolean isUpload, boolean isAuthenticated) {
        // A chave única garante que o limite seja específico por usuário E por operação
        String key = identifier + (isUpload ? "_UPLOAD" : "_QUERY");

        return cache.computeIfAbsent(key, k -> {
            Bandwidth limit = getLimit(isUpload, isAuthenticated);
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private Bandwidth getLimit(boolean isUpload, boolean isAuthenticated) {
        if (isAuthenticated) {
            return isUpload ? authUploadLimit : authQueryLimit;
        } else {
            return isUpload ? anonUploadLimit : anonQueryLimit;
        }
    }

    public Map<String, Integer> getQuotaStatus(String identifier, boolean isAuthenticated) {

        int maxUploads = (int) getLimit(true, isAuthenticated).getCapacity();
        int maxQueries = (int) getLimit(false, isAuthenticated).getCapacity();

        Bucket uploadBucket = resolveBucket(identifier, true, isAuthenticated);
        Bucket queryBucket = resolveBucket(identifier, false, isAuthenticated);
        
        long availableUploads = uploadBucket.getAvailableTokens();
        long availableQueries = queryBucket.getAvailableTokens();

        int usedUploads = (int) (maxUploads - availableUploads);
        int usedQueries = (int) (maxQueries - availableQueries);

        Map<String, Integer> map = new HashMap<>();
        map.put("maxUploads", maxUploads);
        map.put("maxQueries", maxQueries);
        map.put("usedUploads", usedUploads);
        map.put("usedQueries", usedQueries);
        return map;
    }
}
