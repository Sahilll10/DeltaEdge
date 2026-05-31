// Author: Sahil Kumar (Roll: 3252)
package com.DeltaEdge.repository;

import com.DeltaEdge.model.Wallet;
import com.DeltaEdge.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletOrderByTimestampDesc(Wallet wallet);
    List<WalletTransaction> findByWallet(Wallet wallet);
}