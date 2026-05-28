package com.DeltaEdge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.repository.CoinRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CoinServiceImpl implements CoinService {

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CurrencyService currencyService;

    private static final String COIN_CACHE_KEY = "COIN_LIST_PAGE_";

    @Override
    @CircuitBreaker(name = "coinService", fallbackMethod = "fallbackGetCoinList")
    public List<Coin> getCoinList(int page) throws Exception {
        String cacheKey = COIN_CACHE_KEY + page;

        // 1. Try Redis Cache
        List<Coin> cachedCoins = (List<Coin>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCoins != null) {
            log.info("Redis Cache Hit: {}", cacheKey);
            return cachedCoins;
        }

        log.info("Redis Cache Miss. Fetching from CoinGecko...");
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=10&page=" + page;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            List<Coin> coinList = objectMapper.readValue(response.getBody(), new TypeReference<List<Coin>>() {});

            // 2. CRITICAL FIX: Save to local H2 Database so findById works!
            coinRepository.saveAll(coinList);
            log.info("Successfully saved {} coins to H2 Database", coinList.size());

            // 3. Save to Redis Cache
            redisTemplate.opsForValue().set(cacheKey, coinList, 5, TimeUnit.MINUTES);

            return coinList;
        } catch (Exception e) {
            log.error("API Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Coin findById(String coinId) throws Exception {
        return coinRepository.findById(coinId).orElseThrow(() -> new Exception("Coin Not Found"));
    }

    // Fallback for Circuit Breaker
    public List<Coin> fallbackGetCoinList(int page, Throwable t) {
        log.warn("Circuit Breaker Active. Returning data from Local Database.");
        return coinRepository.findAll().stream().limit(10).toList();
    }

    @Override
    public String getMarketChart(String coinId, int days) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart?vs_currency=usd&days=" + days;
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public String getCoinDetails(String coinId) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            BigDecimal inrRate = currencyService.getUsdToInrRate();
            Coin coin = new Coin();

            coin.setId(safeGetText(root, "id"));
            coin.setName(safeGetText(root, "name"));
            coin.setSymbol(safeGetText(root, "symbol"));
            coin.setImage(root.path("image").path("large").asText());

            if (root.has("market_data")) {
                JsonNode marketData = root.get("market_data");
                double rate = inrRate.doubleValue();

                coin.setCurrentPrice(safeGetNestedDouble(marketData, "current_price", "usd") * rate);
                coin.setHigh24h(safeGetNestedDouble(marketData, "high_24h", "usd") * rate);
                coin.setLow24h(safeGetNestedDouble(marketData, "low_24h", "usd") * rate);
                coin.setMarketCap((long) (safeGetNestedLong(marketData, "market_cap", "usd") * rate));
                coin.setMarketCapRank(root.path("market_cap_rank").asInt());
            }

            coinRepository.save(coin);
            messagingTemplate.convertAndSend("/topic/prices/" + coinId, coin);

            return response.getBody();
        } catch (Exception e) {
            log.error("Conversion Error for {}: {}", coinId, e.getMessage());
            throw new Exception("Exchange rate service unavailable.");
        }
    }

    @Override
    public String searchCoin(String keyword) throws Exception {
        return restTemplate.getForObject("https://api.coingecko.com/api/v3/search?query=" + keyword, String.class);
    }

    @Override
    public String getTop50CoinsByMarketCapRank() throws Exception {
        return restTemplate.getForObject("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&page=1", String.class);
    }

    @Override
    public String getTradingCoins() throws Exception {
        return restTemplate.getForObject("https://api.coingecko.com/api/v3/search/trending", String.class);
    }

    private String safeGetText(JsonNode node, String field) {
        return (node != null && node.has(field) && !node.get(field).isNull()) ? node.get(field).asText() : "";
    }

    private double safeGetNestedDouble(JsonNode parent, String f1, String f2) {
        return (parent != null && parent.has(f1) && parent.get(f1).has(f2)) ? parent.get(f1).get(f2).asDouble() : 0.0;
    }

    private long safeGetNestedLong(JsonNode parent, String f1, String f2) {
        return (parent != null && parent.has(f1) && parent.get(f1).has(f2)) ? parent.get(f1).get(f2).asLong() : 0L;
    }
}