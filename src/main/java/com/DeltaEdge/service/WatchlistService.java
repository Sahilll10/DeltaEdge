package com.DeltaEdge.service;

import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Watchlist;

public interface WatchlistService {

    Watchlist findUserWatchlist(Long userId) throws Exception;
    Watchlist createWatchlist(User user);
    Watchlist findById(Long id) throws Exception;
    Watchlist addItemToWatchlist(Coin coin, User user) throws Exception;
    Watchlist removeItemFromWatchlist(Coin coin, User user) throws Exception;
}
