package com.DeltaEdge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
public class CoinController {

    @Autowired
    private CoinService coinService;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. GET ALL COINS
    @GetMapping
    ResponseEntity<List<Coin>> getCoinList(
            @RequestParam(value = "page", defaultValue = "1", required = false) int page
    ) throws Exception {
        List<Coin> coins = coinService.getCoinList(page);
        return new ResponseEntity<>(coins, HttpStatus.ACCEPTED);
    }

    // 2. GET COIN BY ID (FROM LOCAL DB) -> FIX FOR THE 404 CRASH
    @GetMapping("/{coinId}")
    ResponseEntity<Coin> getCoinById(@PathVariable String coinId) throws Exception {
        Coin coin = coinService.findById(coinId);
        return ResponseEntity.ok(coin);
    }

    // 3. GET COIN DETAILS (FROM COINGECKO) -> ROUTE ALIGNED
    @GetMapping("/{coinId}/coin-details")
    ResponseEntity<JsonNode> getCoinDetails(@PathVariable String coinId) throws Exception {
        String coin = coinService.getCoinDetails(coinId);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return ResponseEntity.ok(jsonNode);
    }

    // 4. GET MARKET CHART
    @GetMapping("/{coinId}/chart")
    ResponseEntity<JsonNode> getMarketChart(
            @PathVariable String coinId,
            @RequestParam("days") int days
    ) throws Exception {
        String res = coinService.getMarketChart(coinId, days);
        JsonNode jsonNode = objectMapper.readTree(res);
        return new ResponseEntity<>(jsonNode, HttpStatus.ACCEPTED);
    }

    // 5. SEARCH COINS
    @GetMapping("/search")
    ResponseEntity<JsonNode> searchCoin(@RequestParam("q") String keyword) throws Exception {
        String coin = coinService.searchCoin(keyword);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return ResponseEntity.ok(jsonNode);
    }

    // 6. TOP 50 COINS -> ROUTE ALIGNED
    @GetMapping("/top50")
    ResponseEntity<JsonNode> getTop50CoinByMarketCapRank() throws Exception {
        String coin = coinService.getTop50CoinsByMarketCapRank();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return ResponseEntity.ok(jsonNode);
    }

    // 7. TRENDING COINS -> TYPO FIXED (trading -> trending)
    @GetMapping("/trending")
    ResponseEntity<JsonNode> getTrendingCoins() throws Exception {
        String coin = coinService.getTradingCoins();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return ResponseEntity.ok(jsonNode);
    }
}