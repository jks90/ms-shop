package com.makingcleancode.shop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makingcleancode.shop.entity.RequestIdempotency;
import com.makingcleancode.shop.exception.IdempotencyConflictException;
import com.makingcleancode.shop.repository.RequestIdempotencyRepository;
import com.makingcleancode.shop.util.JsonHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RequestIdempotencyRepository repository;
    private final JsonHashUtil jsonHashUtil;
    private final ObjectMapper objectMapper;

    public <T> CachedResponse<T> resolve(
            Long authUserId,
            String endpoint,
            String idempotencyKey,
            Object requestBody,
            Class<T> responseClass
    ) {
        String requestHash = jsonHashUtil.sha256(requestBody);

        return repository.findByAuthUserIdAndEndpointAndIdempotencyKey(authUserId, endpoint, idempotencyKey)
                .map(record -> {
                    if (!record.getRequestHash().equals(requestHash)) {
                        throw new IdempotencyConflictException(
                                "Idempotency key already used with a different request payload"
                        );
                    }

                    if (record.getResponseBody() == null || record.getResponseStatusCode() == null) {
                        throw new IdempotencyConflictException(
                                "Idempotent request is already in progress"
                        );
                    }

                    try {
                        T body = objectMapper.readValue(record.getResponseBody(), responseClass);
                        return CachedResponse.<T>builder()
                                .replayed(true)
                                .statusCode(record.getResponseStatusCode())
                                .body(body)
                                .record(record)
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Cannot deserialize cached idempotent response", e);
                    }
                })
                .orElseGet(() -> {
                    RequestIdempotency record = new RequestIdempotency();
                    record.setAuthUserId(authUserId);
                    record.setEndpoint(endpoint);
                    record.setIdempotencyKey(idempotencyKey);
                    record.setRequestHash(requestHash);

                    RequestIdempotency saved = repository.save(record);

                    return CachedResponse.<T>builder()
                            .replayed(false)
                            .record(saved)
                            .build();
                });
    }

    public void storeResponse(
            RequestIdempotency record,
            ResponseEntity<?> responseEntity,
            String resourceType,
            Long resourceId
    ) {
        try {
            record.setResponseStatusCode(responseEntity.getStatusCode().value());
            record.setResponseBody(objectMapper.writeValueAsString(responseEntity.getBody()));
            record.setResourceType(resourceType);
            record.setResourceId(resourceId);
            repository.save(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize idempotent response", e);
        }
    }

    @lombok.Builder
    @lombok.Value
    public static class CachedResponse<T> {
        boolean replayed;
        Integer statusCode;
        T body;
        RequestIdempotency record;
    }
}
