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
        BigDecimal amountToAdd = BigDecimal.valueOf(money);
        wallet.setBalance(wallet.getBalance().add(amountToAdd));

        createTransaction(wallet, WalletTransactionType.ADD_MONEY, null, "Deposit to Wallet", amountToAdd);
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws Exception {
        Wallet senderWallet = walletRepository.findByUserIdWithLock(sender.getId());
        BigDecimal transferAmount = BigDecimal.valueOf(amount);

        if (senderWallet.getId().equals(receiverWallet.getId())) {
            throw new Exception("Cannot transfer money to yourself.");
        }

        if (senderWallet.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient Balance.");
        }

        // Deduct from sender
        senderWallet.setBalance(senderWallet.getBalance().subtract(transferAmount));
        walletRepository.save(senderWallet);
        createTransaction(senderWallet, WalletTransactionType.WALLET_TRANSFER, receiverWallet.getId().toString(),
                "Sent to " + receiverWallet.getUser().getFullName(), transferAmount.negate());

        // Add to receiver
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferAmount));
        walletRepository.save(receiverWallet);
        createTransaction(receiverWallet, WalletTransactionType.WALLET_TRANSFER, senderWallet.getId().toString(),
                "Received from " + sender.getFullName(), transferAmount);

        return senderWallet;
    }

    @Override
    @Transactional
    public Wallet payOrderPayment(Order order, User user) {
        Wallet wallet = walletRepository.findByUserIdWithLock(user.getId());

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