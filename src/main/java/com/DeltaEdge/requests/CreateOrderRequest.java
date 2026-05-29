package com.DeltaEdge.requests;

import com.DeltaEdge.domain.OrderType;
import lombok.Data;


@Data
public class CreateOrderRequest{
   private String coinId;
   private double quantity;
   private OrderType orderType;
}
