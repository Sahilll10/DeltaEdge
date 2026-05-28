package com.DeltaEdge.repository;


import com.DeltaEdge.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    public VerificationCode findByUserId(Long UserId);
}
