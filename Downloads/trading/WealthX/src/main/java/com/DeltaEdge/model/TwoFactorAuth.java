package com.DeltaEdge.model;


import com.DeltaEdge.domain.VerificationType;
import lombok.Data;

@Data
public class TwoFactorAuth {
    private boolean isEnabled=false;
    private VerificationType sendTo;

}
