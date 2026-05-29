package com.DeltaEdge.service;

import com.DeltaEdge.domain.WalletTransactionType;
import com.DeltaEdge.model.Wallet;
import com.DeltaEdge.model.WalletTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface WalletTransactionService {


    WalletTransaction createTransaction(Wallet wallet,
                                        WalletTransactionType type,
                                        String transferId,
                                        String purpose,
                                        BigDecimal amount);

    List<WalletTransaction> getTransactionsByWallet(Wallet wallet);
}
