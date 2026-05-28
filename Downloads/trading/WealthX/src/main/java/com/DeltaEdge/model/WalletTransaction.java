// Author: Sahil Kumar (Roll: 3252)
package com.DeltaEdge.model;

import com.DeltaEdge.domain.WalletTransactionType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    private WalletTransactionType type;

    private LocalDateTime timestamp;

    private String transferId;

    private String purpose;

    private BigDecimal amount;
}