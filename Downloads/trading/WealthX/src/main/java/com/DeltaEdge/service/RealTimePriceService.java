package com.DeltaEdge.service;

import com.DeltaEdge.model.Coin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RealTimePriceService {

    @Autowired
    private CoinService coinService;

    @Scheduled(fixedRate = 10000)
    public void updatePrices() throws Exception {
        List<Coin> topCoins = coinService.getCoinList(1);
        for (Coin coin : topCoins) {
            coinService.getCoinDetails(coin.getId());
        }
    }
}