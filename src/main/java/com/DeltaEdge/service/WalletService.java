package com.DeltaEdge.service;

import com.DeltaEdge.model.Wallet;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Order;


public interface WalletService {
    Wallet getUserWallet(User user);
    Wallet addBalanceToWallet(Wallet wallet, Long money);
    Wallet findWalletById(Long id) throws Exception;
    Wallet walletToWalletTransfer(User sender, Wallet rece3iverWallet, Long amount) throws Exception;
    Wallet payOrderPayment(Order order , User user) throws Exception;
}
