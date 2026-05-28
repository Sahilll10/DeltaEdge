package com.DeltaEdge.repository;

import com.DeltaEdge.model.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Long> {
 PaymentDetails  findByUserId(Long userId);

}
