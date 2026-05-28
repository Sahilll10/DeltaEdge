package com.DeltaEdge.service;

import com.DeltaEdge.domain.OrderType;
import com.DeltaEdge.domain.WalletTransactionType;
import com.DeltaEdge.exception.InsufficientBalanceException;
import com.DeltaEdge.model.*;
import com.DeltaEdge.repository.WalletRepository;
import com.DeltaEdge.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Override
    @Transactional
    public Wallet getUserWallet(User user) {
        Wallet wallet = walletRepository.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            wallet = walletRepository.save(wallet);
        }
        return wallet;
    }

    @Override
    @Transactional
    public Wallet addBalanceToWallet(Wallet wallet, Long money) {
        // Fetch with lock to ensure no other thread is adding/subtracting simultaneously
        Wallet lockedWallet = walletRepository.findByUserIdWithLock(wallet.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal amountToAdd = BigDecimal.valueOf(money);
        lockedWallet.setBalance(lockedWallet.getBalance().add(amountToAdd));

        createTransaction(lockedWallet, WalletTransactionType.ADD_MONEY, null, "Deposit to Wallet", amountToAdd);
        return walletRepository.save(lockedWallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(amount);

        // 1. Deadlock Prevention: Always lock the smaller ID first
        Wallet firstLock;
        Wallet secondLock;

        Long senderId = sender.getId();
        Long receiverUserId = receiverWallet.getUser().getId();

        if (senderId.equals(receiverUserId)) {
            throw new Exception("Self-transfer is not permitted.");
        }

        if (senderId < receiverUserId) {
            firstLock = walletRepository.findByUserIdWithLock(senderId).get();
            secondLock = walletRepository.findByUserIdWithLock(receiverUserId).get();
        } else {
            firstLock = walletRepository.findByUserIdWithLock(receiverUserId).get();
            secondLock = walletRepository.findByUserIdWithLock(senderId).get();
        }

        // Identify which locked object is the sender
        Wallet senderWallet = (firstLock.getUser().getId().equals(senderId)) ? firstLock : secondLock;
        Wallet targetWallet = (senderWallet == firstLock) ? secondLock : firstLock;

        // 2. Consistency Check (Atomic)
        if (senderWallet.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient Balance for transfer.");
        }

        // 3. Execution (Atomic)
        senderWallet.setBalance(senderWallet.getBalance().subtract(transferAmount));
        targetWallet.setBalance(targetWallet.getBalance().add(transferAmount));

        walletRepository.save(senderWallet);
        walletRepository.save(targetWallet);

        createTransaction(senderWallet, WalletTransactionType.WALLET_TRANSFER, targetWallet.getId().toString(),
                "Sent to " + targetWallet.getUser().getFullName(), transferAmount.negate());

        createTransaction(targetWallet, WalletTransactionType.WALLET_TRANSFER, senderWallet.getId().toString(),
                "Received from " + sender.getFullName(), transferAmount);

        return senderWallet;
    }

    @Override
    @Transactional
    public Wallet payOrderPayment(Order order, User user) {
        // Acquire Pessimistic Lock immediately
        Wallet wallet = walletRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new InsufficientBalanceException("Wallet context missing."));

        if (order.getOrderType().equals(OrderType.BUY)) {
            if (wallet.getBalance().compareTo(order.getPrice()) < 0) {
                throw new InsufficientBalanceException("Insufficient funds for this trade.");
            }
            wallet.setBalance(wallet.getBalance().subtract(order.getPrice()));
            createTransaction(wallet, WalletTransactionType.BUY_ASSET, order.getId().toString(),
                    "Buy Order: " + order.getOrderItem().getCoin().getSymbol(), order.getPrice().negate());
        } else {
            wallet.setBalance(wallet.getBalance().add(order.getPrice()));
            createTransaction(wallet, WalletTransactionType.SELL_ASSET, order.getId().toString(),
                    "Sell Order: " + order.getOrderItem().getCoin().getSymbol(), order.getPrice());
        }

        return walletRepository.save(wallet);
    }

    private void createTransaction(Wallet wallet, WalletTransactionType type, String transferId, String purpose, BigDecimal amount) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setTransferId(transferId);
        transaction.setPurpose(purpose);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Override
    public Wallet findWalletById(Long id) throws Exception {
        return walletRepository.findById(id).orElseThrow(() -> new Exception("Wallet not found."));
    }
}