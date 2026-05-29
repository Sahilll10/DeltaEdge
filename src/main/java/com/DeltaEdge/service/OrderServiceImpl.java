package com.DeltaEdge.service;

import com.DeltaEdge.domain.OrderStatus;
import com.DeltaEdge.domain.OrderType;
import com.DeltaEdge.exception.InvalidOrderException;
import com.DeltaEdge.exception.ResourceNotFoundException;
import com.DeltaEdge.model.*;
import com.DeltaEdge.repository.OrderItemRepository;
import com.DeltaEdge.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public Order createOrder(User user, OrderItem orderItem, OrderType orderType) {
        BigDecimal coinPrice = BigDecimal.valueOf(orderItem.getCoin().getCurrentPrice());
        BigDecimal quantity = BigDecimal.valueOf(orderItem.getQuantity());
        BigDecimal totalPrice = coinPrice.multiply(quantity);

        Order order = new Order();
        order.setUser(user);
        order.setOrderItem(orderItem);
        order.setOrderType(orderType);
        order.setPrice(totalPrice);
        order.setTimestamp(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order buyAsset(Coin coin, double quantity, User user) {
        if (quantity <= 0) throw new InvalidOrderException("Quantity must be > 0");

        try {
            double buyPrice = coin.getCurrentPrice();
            OrderItem orderItem = new OrderItem();
            orderItem.setCoin(coin);
            orderItem.setQuantity(quantity);
            orderItem.setBuyPrice(buyPrice);
            orderItem = orderItemRepository.save(orderItem);

            Order order = createOrder(user, orderItem, OrderType.BUY);
            orderItem.setOrder(order);

            walletService.payOrderPayment(order, user);

            Asset oldAsset = assetService.findAssetByUserIdAndCoinId(user.getId(), coin.getId());
            if (oldAsset == null) {
                assetService.createAsset(user, coin, quantity);
            } else {
                assetService.updateAsset(oldAsset.getId(), quantity);
            }

            order.setStatus(OrderStatus.SUCCESS);
            Order savedOrder = orderRepository.save(order);
            String ipAddress = request.getRemoteAddr();

            auditService.logEvent(user.getId(), "ORDER_ACTION", "SUCCESS", "Order details", ipAddress);

            return savedOrder;
        } catch (Exception e) {
            auditService.logEvent(user.getId(), "ORDER", "SUCCESS", "Details", request.getRemoteAddr());
            throw new InvalidOrderException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Order sellAsset(Coin coin, double quantity, User user) {
        if (quantity <= 0) throw new InvalidOrderException("Quantity must be > 0");

        try {
            Asset assetToSell = assetService.findAssetByUserIdAndCoinId(user.getId(), coin.getId());
            if (assetToSell == null || assetToSell.getQuantity() < quantity) {
                throw new InvalidOrderException("Insufficient asset quantity");
            }

            double buyPrice = assetToSell.getBuyPrice();
            double sellPrice = coin.getCurrentPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setCoin(coin);
            orderItem.setQuantity(quantity);
            orderItem.setBuyPrice(buyPrice);
            orderItem.setSellPrice(sellPrice);
            orderItem = orderItemRepository.save(orderItem);

            Order order = createOrder(user, orderItem, OrderType.SELL);
            orderItem.setOrder(order);

            walletService.payOrderPayment(order, user);

            Asset updatedAsset = assetService.updateAsset(assetToSell.getId(), -quantity);
            if (updatedAsset.getQuantity() <= 0.0001) {
                assetService.deleteAsset(updatedAsset.getId());
            }

            order.setStatus(OrderStatus.SUCCESS);
            Order savedOrder = orderRepository.save(order);

            // BANKING AUDIT: Success Log
            auditService.logEvent(user.getId(), "ORDER", "SUCCESS", "Details", request.getRemoteAddr());

            return savedOrder;
        } catch (Exception e) {
            // BANKING AUDIT: Failure Log
            auditService.logEvent(user.getId(), "ORDER", "SUCCESS", "Details", request.getRemoteAddr());
            throw new InvalidOrderException(e.getMessage());
        }
    }

    @Override
    public Order processOrder(Coin coin, double quantity, OrderType orderType, User user) {
        if (orderType == OrderType.BUY) return buyAsset(coin, quantity, user);
        return sellAsset(coin, quantity, user);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not Found!!"));
    }

    @Override
    public List<Order> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order executeOrder(User user, Coin coin, double quantity, OrderType orderType) {
        return processOrder(coin, quantity, orderType, user);
    }
}