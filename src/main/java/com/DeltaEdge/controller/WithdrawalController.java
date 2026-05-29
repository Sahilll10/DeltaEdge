package com.DeltaEdge.controller;

import com.DeltaEdge.domain.WalletTransactionType;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Wallet;
import com.DeltaEdge.model.Withdrawal;
import com.DeltaEdge.service.UserService;
import com.DeltaEdge.service.WalletService;
import com.DeltaEdge.service.WalletTransactionService;
import com.DeltaEdge.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/withdrawal")
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private WalletService walletService;

    @PostMapping("/{amount}")
    public ResponseEntity<Withdrawal> withdrawalRequest(
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        Wallet userWallet = walletService.getUserWallet(user);

        // Fintech Upgrade: Convert Long to BigDecimal for service compatibility
        BigDecimal withdrawalAmount = BigDecimal.valueOf(amount);

        Withdrawal withdrawal = withdrawalService.requestWithdrawal(withdrawalAmount, user);

        // Deduct balance (using negative value)
        walletService.addBalanceToWallet(userWallet, -amount);

        // Record in the Audit Ledger
        walletTransactionService.createTransaction(
                userWallet,
                WalletTransactionType.WITHDRAWAL,
                null,
                "Bank Account Withdrawal",
                withdrawalAmount.negate()
        );

        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    @PatchMapping("/admin/withdrawal/{id}/proceed/{accept}")
    public ResponseEntity<Withdrawal> proceedWithdrawal(
            @PathVariable Long id,
            @PathVariable boolean accept,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        Withdrawal withdrawal = withdrawalService.proceedWithWithdrawal(id, accept);
        Wallet userWallet = walletService.getUserWallet(user);

        if (!accept) {
            // Refund the balance if admin rejects the withdrawal
            walletService.addBalanceToWallet(userWallet, withdrawal.getAmount().longValue());
        }
        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Withdrawal>> getWithdrawalHistory(
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        List<Withdrawal> withdrawal = withdrawalService.getUserWithdrawalHistory(user);
        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Withdrawal>> getAllWithdrawalRequest(
            @RequestHeader("Authorization") String jwt) throws Exception {
        userService.findUserProfileByJwt(jwt);
        List<Withdrawal> withdrawal = withdrawalService.getAllWithdrawalRequest();
        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }
}