package com.DeltaEdge.controller;


import com.DeltaEdge.domain.PaymentGateway;
import com.DeltaEdge.model.PaymentOrder;
import com.DeltaEdge.model.User;
import com.DeltaEdge.response.PaymentResponse;
import com.DeltaEdge.service.PaymentService;
import com.DeltaEdge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {


    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(
            @PathVariable PaymentGateway paymentGateway,
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        PaymentResponse paymentResponse;

        PaymentOrder order = paymentService.createOrder(user, amount, paymentGateway);
        if (paymentGateway.equals(PaymentGateway.RAZORPAY)) {
            paymentResponse = paymentService.createRazorpayPaymentLink(user, amount);
        }
        else{
            throw new Exception("Unsupported Payment Method");
        }
        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }
}
