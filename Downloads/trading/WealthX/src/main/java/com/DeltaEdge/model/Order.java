package com.DeltaEdge.model;


import com.DeltaEdge.domain.OrderStatus;
import com.DeltaEdge.domain.OrderType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="orders") //BECAUSE "ORDER" IS ALREADY A KEYWORD
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;

    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private BigDecimal price;

    private LocalDateTime timestamp= LocalDateTime.now();

    @Column(nullable = false)
    private OrderStatus status;


//         NEEWWW IIIMMPPP CASCADE
    @OneToOne(mappedBy = "order", cascade=CascadeType.ALL)
    private OrderItem orderItem;



}
