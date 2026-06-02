package com.DeltaEdge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.repository.CoinRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CoinServiceImpl implements CoinService {

    @Value("${coingecko.api.key}")
    private String apiKey;

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
    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "coinService", fallbackMethod = "fallbackGetCoinList")
    public List<Coin> getCoinList(int page) throws Exception {
        String cacheKey = COIN_CACHE_KEY + page;

        try {
            List<Coin> cachedCoins = (List<Coin>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCoins != null) {
                log.info("Redis Cache Hit: {}", cacheKey);
                return cachedCoins;
            }
        } catch (Exception e) {
            log.warn("Redis is offline. Bypassing cache -> {}", e.getMessage());
        }

        log.info("Fetching from CoinGecko...");
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=inr&per_page=10&page=" + page
                + "&x_cg_demo_api_key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            List<Coin> coinList = objectMapper.readValue(response.getBody(), new TypeReference<List<Coin>>() {});

            coinRepository.saveAll(coinList);
            log.info("Successfully saved {} coins to Database", coinList.size());

            try {
                redisTemplate.opsForValue().set(cacheKey, coinList, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Could not save to Redis, but API data was fetched successfully.");
            }

            return coinList;
        } catch (Exception e) {
            log.error("API Error: {}", e.getMessage());
            throw e;
        }
    }

    // THE FAILSAFE DATABASE HYDRATION FIX
    @Override
    public Coin findById(String coinId) {
        return coinRepository.findById(coinId).orElseGet(() -> {
            log.warn("Coin {} missing from H2 database! Auto-hydrating...", coinId);
            try {
                getCoinDetails(coinId);
                return coinRepository.findById(coinId).get();
            } catch (Exception e) {
                log.error("CoinGecko API block or failure. Generating safe placeholder for UI.");
                // Ultimate Failsafe: Return a mock coin so the graph NEVER crashes
                Coin placeholder = new Coin();
                placeholder.setId(coinId);
                placeholder.setName(coinId.substring(0, 1).toUpperCase() + coinId.substring(1));
                placeholder.setSymbol(coinId.length() >= 3 ? coinId.substring(0, 3).toUpperCase() : coinId.toUpperCase());
                placeholder.setCurrentPrice(1.0);
                placeholder.setImage("https://cdn-icons-png.flaticon.com/512/825/825508.png"); // Generic crypto icon
                return placeholder;
            }
        });
    }

    public List<Coin> fallbackGetCoinList(int page, Throwable t) {
        log.warn("Circuit Breaker Active. Returning data from Local Database. Reason: {}", t.getMessage());
        return coinRepository.findAll().stream().limit(10).toList();
    }

    @Override
    public String getMarketChart(String coinId, int days) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart?vs_currency=inr&days=" + days
                + "&x_cg_demo_api_key=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public String getCoinDetails(String coinId) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId
                + "?x_cg_demo_api_key=" + apiKey;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            Coin coin = new Coin();

            coin.setId(safeGetText(root, "id"));
            coin.setName(safeGetText(root, "name"));
            coin.setSymbol(safeGetText(root, "symbol"));
            coin.setImage(root.path("image").path("large").asText());

            if (root.has("market_data")) {
                JsonNode marketData = root.get("market_data");

                coin.setCurrentPrice(safeGetNestedDouble(marketData, "current_price", "inr"));
                coin.setHigh24h(safeGetNestedDouble(marketData, "high_24h", "inr"));
                coin.setLow24h(safeGetNestedDouble(marketData, "low_24h", "inr"));
                coin.setMarketCap(safeGetNestedLong(marketData, "market_cap", "inr"));
                coin.setMarketCapRank(root.path("market_cap_rank").asInt());
            }

            coinRepository.save(coin);
            messagingTemplate.convertAndSend("/topic/prices/" + coinId, coin);

            return response.getBody();
        } catch (Exception e) {
            log.error("Conversion Error for {}: {}", coinId, e.getMessage());
            throw new Exception("Coin details fetch failed.");
        }
    }

    @Override
    public String searchCoin(String keyword) throws Exception {
        String url = "https://api.coingecko.com/api/v3/search?query=" + keyword
                + "&x_cg_demo_api_key=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public String getTop50CoinsByMarketCapRank() throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=inr&per_page=50&page=1"
                + "&x_cg_demo_api_key=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public String getTradingCoins() throws Exception {
        String url = "https://api.coingecko.com/api/v3/search/trending"
                + "?x_cg_demo_api_key=" + apiKey;
        return restTemplate.getForObject(url, String.class);
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