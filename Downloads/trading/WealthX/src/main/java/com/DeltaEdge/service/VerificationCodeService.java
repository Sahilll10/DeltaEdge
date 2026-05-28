package com.DeltaEdge.service;

import com.DeltaEdge.domain.VerificationType;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.VerificationCode;


public interface VerificationCodeService {

    VerificationCode sendVerificationCode(User user, VerificationType verificationType);

    VerificationCode getVerificationCodeById(Long id) throws Exception;

    VerificationCode getVerificationCodeByUser(Long userId);



    void deleteVerificationCodeById(VerificationCode verificationCode);

}
