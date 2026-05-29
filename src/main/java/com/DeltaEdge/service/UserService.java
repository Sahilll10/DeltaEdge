package com.DeltaEdge.service;
import com.DeltaEdge.domain.VerificationType;
import com.DeltaEdge.model.User;

public interface UserService {

    public User findUserProfileByJwt(String jwt) throws Exception;
    public User findUserByEmail(String email) throws Exception;
    public User findUserById(Long userId) throws Exception;

//    TO ENABLE 2FA 3 PARAMTERES ARE REQUIRED
    public User enableTwoFactorAuthentication
                                         (VerificationType verificationType,
                                          String sendTo,
                                          User user);

    User updatePassword(User user, String newPassword);

}
