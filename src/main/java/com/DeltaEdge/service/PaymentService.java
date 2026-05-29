package com.DeltaEdge.service;

import com.DeltaEdge.domain.PaymentGateway;
import com.DeltaEdge.model.PaymentOrder;
import com.DeltaEdge.model.User;
import com.DeltaEdge.response.PaymentResponse;

public interface PaymentService {

    PaymentOrder createOrder(User user, Long amount, PaymentGateway paymentGateway);

        PaymentOrder getPaymentOrderById(Long id) throws Exception;

        Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws Exception;

        PaymentResponse createRazorpayPaymentLink(User user, Long amount) throws Exception;
}
