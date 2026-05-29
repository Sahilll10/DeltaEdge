package com.DeltaEdge.service;

import com.DeltaEdge.model.PaymentDetails;
import com.DeltaEdge.model.User;


public interface PaymentDetailsService {
    PaymentDetails addPaymentDetails(String accountNumber,
                                            String accountHolderName,
                                            String ifsc,
                                            String bankName,
                                            User user);

     PaymentDetails getUserPaymentDetails(User user);


}
