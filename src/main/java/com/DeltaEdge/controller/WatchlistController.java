package com.DeltaEdge.controller;

import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Watchlist;
import com.DeltaEdge.service.CoinService;
import com.DeltaEdge.service.UserService;
import com.DeltaEdge.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    // FIX 1: Removed "/user" to perfectly match the frontend route
    @GetMapping
    public ResponseEntity<Watchlist> getUserWatchlist(
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Watchlist watchlist = watchlistService.findUserWatchlist(user.getId());
        return ResponseEntity.ok(watchlist);
    }

    @PostMapping("/create")
    public ResponseEntity<Watchlist> createWatchlist(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Watchlist watchlist = watchlistService.createWatchlist(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlist);
    }

    @GetMapping("/{watchlistId}")
    public ResponseEntity<Watchlist> getWatchlistById(
            @PathVariable Long watchlistId) throws Exception {
        Watchlist watchlist = watchlistService.findById(watchlistId);
        return ResponseEntity.ok(watchlist);
    }

    @PatchMapping("/add/coin/{coinId}")
    public ResponseEntity<Watchlist> addItemToWatchlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Coin coin = coinService.findById(coinId);
        Watchlist watchlist = watchlistService.addItemToWatchlist(coin, user);
        return ResponseEntity.ok(watchlist);
    }

    // FIX 2: Added the completely missing REMOVE endpoint
    @DeleteMapping("/remove/coin/{coinId}")
    public ResponseEntity<Watchlist> removeItemFromWatchlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Coin coin = coinService.findById(coinId);
        // Ensure your WatchlistService has this method name!
        Watchlist watchlist = watchlistService.removeItemFromWatchlist(coin, user);
        return ResponseEntity.ok(watchlist);
    }
}