package com.DeltaEdge.controller;

import com.DeltaEdge.domain.OrderType;
import com.DeltaEdge.model.Coin;
import com.DeltaEdge.model.Order;
import com.DeltaEdge.model.User;
import com.DeltaEdge.requests.CreateOrderRequest;
import com.DeltaEdge.service.CoinService;
import com.DeltaEdge.service.OrderService;
import com.DeltaEdge.service.UserService;
import com.DeltaEdge.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private IdempotencyService idempotencyService;

    @PostMapping("/pay")
    public ResponseEntity<?> payOrderPayment(
            @RequestHeader("Authorization") String jwt,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderRequest req
    ) throws Exception {

        // 1. Clean the Token once
        String cleanToken = (jwt != null && jwt.startsWith("Bearer ")) ? jwt.substring(7).trim() : jwt;

        // 2. Check Idempotency BEFORE processing
        if (idempotencyKey != null) {
            if (idempotencyService.isDuplicate(idempotencyKey)) {
                String prevResponse = idempotencyService.getPreviousResponse(idempotencyKey);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Duplicate request. Previous Status: " + prevResponse);
            }
        }

        try {
            // 3. Fetch User using the CLEAN token
            User user = userService.findUserProfileByJwt(cleanToken);

            if (user == null) {
                if (idempotencyKey != null) idempotencyService.removeKey(idempotencyKey);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User session invalid or database reset. Please Sign Up/Sign In again.");
            }

            // 4. Fetch Coin
            Coin coin = coinService.findById(req.getCoinId());

            // 5. Process Order
            Order order = orderService.processOrder(coin, req.getQuantity(), req.getOrderType(), user);

            // 6. Update Idempotency on success
            if (idempotencyKey != null) {
                idempotencyService.updateStatus(idempotencyKey, "SUCCESS: OrderID " + order.getId());
            }

            return ResponseEntity.ok(order);

        } catch (Exception e) {
            // 7. Cleanup Idempotency if business logic fails
            if (idempotencyKey != null) {
                idempotencyService.removeKey(idempotencyKey);
            }
            // Log the actual error for debugging
            System.err.println("Order Processing Error: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrdersForUser(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) OrderType order_type,
            @RequestParam(required = false) String asset_symbol
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        List<Order> orders = orderService.getAllOrdersOfUser(user.getId(), order_type, asset_symbol);
        return ResponseEntity.ok(orders);
    }
}