package com.DeltaEdge.service;

import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Watchlist;
import com.DeltaEdge.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Override
    public Watchlist findUserWatchlist(Long userId) throws Exception {
        Watchlist watchlist = watchlistRepository.findByUserId(userId);
        if (watchlist == null) {
            throw new Exception("Watchlist not found for user: " + userId);
        }
        return watchlist;
    }

    @Override
    public Watchlist createWatchlist(User user) {
        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        return watchlistRepository.save(watchlist);
    }

    @Override
    public Watchlist findById(Long id) throws Exception {
        return watchlistRepository.findById(id).orElseThrow(
                () -> new Exception("Watchlist not found with ID: " + id)
        );
    }

    @Override
    public Watchlist addItemToWatchlist(Coin coin, User user) throws Exception {
        Watchlist watchlist = findUserWatchlist(user.getId());

        // Toggle
        if (watchlist.getCoins().contains(coin)) {
            watchlist.getCoins().remove(coin);
        } else {
            watchlist.getCoins().add(coin);
        }
        return watchlistRepository.save(watchlist);
    }

    @Override
    public Watchlist removeItemFromWatchlist(Coin coin, User user) throws Exception {
        Watchlist watchlist = findUserWatchlist(user.getId());
        watchlist.getCoins().remove(coin);
        return watchlistRepository.save(watchlist);
    }
}