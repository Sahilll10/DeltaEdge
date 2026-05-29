package com.DeltaEdge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GraphDataSyncService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CorrelationService correlationService;

    public List<Double> fetchHistoricalPrices(String coinId) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart?vs_currency=usd&days=30&interval=daily";

        System.out.println("Fetching historical data for: " + coinId);
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode pricesNode = root.get("prices");

        List<Double> prices = new ArrayList<>();
        if (pricesNode != null && pricesNode.isArray()) {
            for (JsonNode node : pricesNode) {
                prices.add(node.get(1).asDouble());
            }
        }
        return prices;
    }

    public void buildGraphFromCoinGecko() throws Exception {
        String baseCoin = "bitcoin";
        List<String> altcoins = Arrays.asList("ethereum", "solana", "dogecoin", "ripple", "tron", "usd-coin");
        List<Double> basePrices = fetchHistoricalPrices(baseCoin);
        for (String altcoin : altcoins) {
            try {
                List<Double> altPrices = fetchHistoricalPrices(altcoin);

                int minSize = Math.min(basePrices.size(), altPrices.size());
                List<Double> alignedBase = basePrices.subList(0, minSize);
                List<Double> alignedAlt = altPrices.subList(0, minSize);

                correlationService.updateEdge(baseCoin, altcoin, alignedBase, alignedAlt);
                System.out.println("Successfully calculated edge: " + baseCoin + " <-> " + altcoin);
                Thread.sleep(3000);

            } catch (Exception e) {
                System.err.println("Failed to sync edge for " + altcoin + ": " + e.getMessage());
            }
        }
        System.out.println("Graph Synchronization Complete.");
    }
}