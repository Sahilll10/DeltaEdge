
package com.DeltaEdge.service;

import com.DeltaEdge.domain.OrderType;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.Order;
import com.DeltaEdge.model.OrderItem;
import com.DeltaEdge.model.User;

import java.util.List;

public interface OrderService {

    Order createOrder(User user, OrderItem orderItem, OrderType orderType);

    Order getOrderById(Long orderId);
    List<Order> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol);

    Order buyAsset(Coin coin, double quantity, User user);

    Order sellAsset(Coin coin, double quantity, User user);

    Order processOrder(Coin coin, double quantity, OrderType orderType, User user);

    Order executeOrder(User user, Coin coin, double quantity, OrderType orderType);
}