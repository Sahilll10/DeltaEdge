package com.DeltaEdge.model;


import com.DeltaEdge.domain.PaymentGateway;
import com.DeltaEdge.domain.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long amount;

    @Enumerated(EnumType.STRING)
//    WHY IS THIS BEING USED


    private PaymentOrderStatus status;


    @Enumerated(EnumType.STRING)
    private PaymentGateway paymentGateway;

    @ManyToOne
    private User user;

}
