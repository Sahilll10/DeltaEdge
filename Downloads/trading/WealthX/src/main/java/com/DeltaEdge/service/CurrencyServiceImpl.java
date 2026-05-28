package com.DeltaEdge.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${exchangerate.api.key}")
    private String apiKey;

    private static final String RATE_CACHE_KEY = "USD_INR_RATE";

    @Override
    public BigDecimal getUsdToInrRate() {
        try {
            Object cachedRate = redisTemplate.opsForValue().get(RATE_CACHE_KEY);
            if (cachedRate != null) {
                log.info("Exchange Rate Cache Hit: {}", cachedRate);
                return new BigDecimal(cachedRate.toString());
            }
        } catch (Exception e) {
            log.warn("Redis connectivity issue: {}", e.getMessage());
        }

        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/USD/INR";

        try {
            log.info("Fetching live USD to INR rate from External API...");
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("conversion_rate")) {
                double rate = response.get("conversion_rate").asDouble();
                redisTemplate.opsForValue().set(RATE_CACHE_KEY, String.valueOf(rate), 1, TimeUnit.HOURS);
                return BigDecimal.valueOf(rate);
            }
        } catch (Exception e) {
            log.error("Forex API error for API_KEY {}: {}", apiKey, e.getMessage());
        }

        log.warn("Using hardcoded fallback rate of 83.50");
        return new BigDecimal("83.50");
    }
}