
package com.DeltaEdge.controller;

import com.DeltaEdge.model.*;
import com.DeltaEdge.service.OrderService;
import com.DeltaEdge.service.PaymentService;
import com.DeltaEdge.service.UserService;
import com.DeltaEdge.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Wallet> getUserWallet(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Wallet wallet = walletService.getUserWallet(user);
        return new ResponseEntity<>(wallet, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{walletId}/transfer")
    public ResponseEntity<Wallet> walletToWalletTransfer(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long walletId,
            @RequestBody WalletTransaction req) throws Exception {

        User senderUser = userService.findUserProfileByJwt(jwt);
        Wallet receiverWallet = walletService.findWalletById(walletId);

        // Fintech Fix: Convert the transaction amount to long if your service still expects Long,
        // OR update the service to take BigDecimal (Recommended).
        // Assuming walletService.walletToWalletTransfer takes Long amount:
        Wallet wallet = walletService.walletToWalletTransfer(
                senderUser,
                receiverWallet,
                req.getAmount().longValue()
        );
        return new ResponseEntity<>(wallet, HttpStatus.ACCEPTED);
    }

    @PutMapping("/order/{orderId}/pay")
    public ResponseEntity<Wallet> payOrderPayment(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Order order = orderService.getOrderById(orderId);
        Wallet wallet = walletService.payOrderPayment(order, user);
        return new ResponseEntity<>(wallet, HttpStatus.ACCEPTED);
    }
    @PutMapping("/deposit")
    public ResponseEntity<Wallet> addBalanceToWallet(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(name = "order_id", required = false) Long orderId,
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestBody(required = false) Map<String, Object> requestBody
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Wallet wallet = walletService.getUserWallet(user);
        if (wallet.getBalance() == null) {
            wallet.setBalance(BigDecimal.ZERO);
        }

        //Direct Test
        if (requestBody != null && requestBody.containsKey("amount")) {
            // Using Number prevents Jackson ClassCastExceptions between Integer and Long
            Number amount = (Number) requestBody.get("amount");
            wallet = walletService.addBalanceToWallet(wallet, amount.longValue());
            return new ResponseEntity<>(wallet, HttpStatus.ACCEPTED);
        }
        //Razorpay Verification Flow
        if (orderId != null && paymentId != null) {
            PaymentOrder order = paymentService.getPaymentOrderById(orderId);
            Boolean status = paymentService.ProceedPaymentOrder(order, paymentId);
            if (status) {
                wallet = walletService.addBalanceToWallet(wallet, order.getAmount());
            }
            return new ResponseEntity<>(wallet, HttpStatus.ACCEPTED);
        }

        throw new Exception("Invalid deposit request: Missing amount or payment credentials.");
    }

    //Added missing Transactions endpoint
    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransaction>> getWalletTransactions(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Wallet wallet = walletService.getUserWallet(user);

        // Use the walletService to fetch transactions instead of the entity!
        List<WalletTransaction> transactions = walletService.getTransactionsByWallet(wallet);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}