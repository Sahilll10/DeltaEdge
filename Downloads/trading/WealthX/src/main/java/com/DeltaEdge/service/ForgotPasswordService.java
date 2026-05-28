package com.DeltaEdge.service;

import com.DeltaEdge.domain.VerificationType;
import com.DeltaEdge.model.ForgotPasswordToken;
import com.DeltaEdge.model.User;

public interface ForgotPasswordService {


    ForgotPasswordToken createToken(User user,
                                    String id, String otp,
                                    VerificationType verificationType,
                                    String sendTo) ;

        ForgotPasswordToken findById(String id);
        ForgotPasswordToken findByUser(Long userId);
        void deleteToken (ForgotPasswordToken token);
}
