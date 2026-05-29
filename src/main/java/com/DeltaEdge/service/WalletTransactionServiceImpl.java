// Author: Sahil Kumar (Roll: 3252)
package com.DeltaEdge.service;

import com.DeltaEdge.domain.WalletTransactionType;
import com.DeltaEdge.model.Wallet;
import com.DeltaEdge.model.WalletTransaction;
import com.DeltaEdge.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletTransactionServiceImpl implements WalletTransactionService {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Override
    public WalletTransaction createTransaction(Wallet wallet, WalletTransactionType type, String transferId, String purpose, BigDecimal amount) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setPurpose(purpose);
        transaction.setTransferId(transferId);
        transaction.setAmount(amount);

        // Match the model update: Use LocalDateTime for audit sequencing
        transaction.setTimestamp(LocalDateTime.now());

        return walletTransactionRepository.save(transaction);
    }

    @Override
    public List<WalletTransaction> getTransactionsByWallet(Wallet wallet) {
        return walletTransactionRepository.findByWalletOrderByTimestampDesc(wallet);
    }
}