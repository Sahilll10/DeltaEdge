package com.DeltaEdge.model;

import com.DeltaEdge.domain.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


   @Enumerated(EnumType.STRING)
//   THIS WAS SOMETHING ERROR FOR @Autowired
    private WithdrawalStatus status;


   private BigDecimal amount;

    @ManyToOne
    private User user;

    private LocalDateTime date= LocalDateTime.now();

}
