package com.DeltaEdge.repository;

import com.DeltaEdge.model.TwoFactorOTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorOtpRepository extends JpaRepository<TwoFactorOTP, String > {
    TwoFactorOTP findByUserId(Long userId);
}
