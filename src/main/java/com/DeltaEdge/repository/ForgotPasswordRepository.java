package com.DeltaEdge.repository;


import com.DeltaEdge.model.ForgotPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRepository extends
        JpaRepository<ForgotPasswordToken,String> {
//ISME STRING USE Kr RHE HAIN KYUkI TOKEN MEI String Id hai

   ForgotPasswordToken findByUserId(Long userId);


}
