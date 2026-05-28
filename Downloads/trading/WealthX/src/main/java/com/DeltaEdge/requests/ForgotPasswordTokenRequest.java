package com.DeltaEdge.requests;


import com.DeltaEdge.domain.VerificationType;
import lombok.Data;

@Data
public class ForgotPasswordTokenRequest {
    private String sendTo;
    private String otp;
    private VerificationType verificationType;
}
