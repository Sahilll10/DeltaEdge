package com.DeltaEdge.service;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.DeltaEdge.domain.PaymentGateway;
import com.DeltaEdge.domain.PaymentOrderStatus;
import com.DeltaEdge.model.PaymentOrder;
import com.DeltaEdge.model.User;
import com.DeltaEdge.repository.PaymentOrderRepository;
import com.DeltaEdge.response.PaymentResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecretKey;

    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentGateway paymentGateway) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setUser(user);
        paymentOrder.setAmount(amount);
        paymentOrder.setPaymentGateway(paymentGateway);
        paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        return paymentOrderRepository.findById(id).orElseThrow(
                () -> new Exception("Payment Order not Found!!")
        );
    }

    @Override
    public Boolean ProceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws Exception {

//        ADDED FOR DepositMoney API
      if(paymentOrder.getStatus()==null){
          paymentOrder.setStatus(PaymentOrderStatus.PENDING);
      }


        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            if (paymentOrder.getPaymentGateway().equals(PaymentGateway.RAZORPAY)) {
                try {
                    RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecretKey);
                    Payment payment = razorpay.payments.fetch(paymentId);
                    Integer amount = payment.get("amount");
                    String status = payment.get("status");

                    if (status.equals("captured")) {
                        paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                        paymentOrderRepository.save(paymentOrder);
                        return true;
                    }
                    paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                    paymentOrderRepository.save(paymentOrder);
                    return false;
                } catch (RazorpayException e) {
                    throw new Exception("Razorpay Error: " + e.getMessage());
                }
            }
            // For non-Razorpay payments
            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true;
        }
        return false;
    }

    @Override
    public PaymentResponse createRazorpayPaymentLink(User user, Long amount) throws Exception {
        Long amountInPaise = amount * 100;

        try {
            RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecretKey);

            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", amountInPaise);
            paymentLinkRequest.put("currency", "INR");

            JSONObject customer = new JSONObject();
            customer.put("name", user.getFullName());
            customer.put("email", user.getEmail());
            paymentLinkRequest.put("customer", customer);

            JSONObject notify = new JSONObject();
            notify.put("email", true);
            paymentLinkRequest.put("notify", notify);

            paymentLinkRequest.put("reminder_enable", true);
            paymentLinkRequest.put("callback_url", "http://localhost:5173/wallet");
            paymentLinkRequest.put("callback_method", "get");

            PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest);

            String paymentLinkId = payment.get("id");
            String paymentLinkUrl = payment.get("short_url");

            PaymentResponse res = new PaymentResponse();
            res.setPaymentUrl(paymentLinkUrl);
            return res;
        } catch (RazorpayException e) {
            System.out.println("Error Creating Payment Link: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}