
package com.DeltaEdge.service;

import com.DeltaEdge.model.User;
import com.DeltaEdge.model.Withdrawal;
import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalService {
    Withdrawal requestWithdrawal(BigDecimal amount, User user);

    Withdrawal proceedWithWithdrawal(Long withdrawalId, boolean accept) throws Exception;

    List<Withdrawal> getUserWithdrawalHistory(User user);

    List<Withdrawal> getAllWithdrawalRequest();
}