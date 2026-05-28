package com.DeltaEdge.response;


import lombok.Data;

@Data
public class PaymentResponse {

    private String paymentUrl;

    public void setPaymentUrl(String paymentUrl){
        this.paymentUrl=paymentUrl;
    }
}
