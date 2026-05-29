package com.DeltaEdge.service;

import com.DeltaEdge.domain.WithdrawalStatus;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Withdrawal;
import com.DeltaEdge.repository.WithdrawalRepository;
import com.DeltaEdge.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Override
    public Withdrawal requestWithdrawal(BigDecimal amount, User user) {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setUser(user);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setDate(LocalDateTime.now());
        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public Withdrawal proceedWithWithdrawal(Long withdrawalId, boolean accept) throws Exception {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal request not found"));

        withdrawal.setDate(LocalDateTime.now());

        if (accept) {
            withdrawal.setStatus(WithdrawalStatus.SUCCESS);
            //In the controller/orchestrator, ensure walletService.deduct() is called here.
        } else {
            withdrawal.setStatus(WithdrawalStatus.DECLINED);
        }

        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public List<Withdrawal> getUserWithdrawalHistory(User user) {
        return withdrawalRepository.findByUserId(user.getId());
    }

    @Override
    public List<Withdrawal> getAllWithdrawalRequest() {
        return withdrawalRepository.findAll();
    }
}