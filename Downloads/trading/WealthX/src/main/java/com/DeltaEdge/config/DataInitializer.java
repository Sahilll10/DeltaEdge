package com.DeltaEdge.config;

import com.DeltaEdge.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CoinService coinService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DeltaEdge: Initializing Crypto Market Data...");
        try {
            // This calls your existing method that hits CoinGecko
            coinService.getCoinList(1);
            System.out.println("DeltaEdge: Successfully fetched and saved coins to H2.");
        } catch (Exception e) {
            System.err.println("DeltaEdge Error: Could not fetch coins on startup: " + e.getMessage());
        }
    }
}