package com.DeltaEdge.controller;


import com.DeltaEdge.requests.ForgotPasswordTokenRequest;
import com.DeltaEdge.domain.VerificationType;
import com.DeltaEdge.model.ForgotPasswordToken;
import com.DeltaEdge.model.User;
import com.DeltaEdge.model.VerificationCode;
import com.DeltaEdge.requests.ResetPasswordRequest;
import com.DeltaEdge.response.ApiResponse;
import com.DeltaEdge.response.AuthResponse;
import com.DeltaEdge.service.EmailService;
import com.DeltaEdge.service.ForgotPasswordService;
import com.DeltaEdge.service.UserService;
import com.DeltaEdge.service.VerificationCodeService;
import com.DeltaEdge.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;


    private String jwt;

    @GetMapping("api/users/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization")String jwt) throws Exception {
        User user=userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }


    @PostMapping("/api/users/verification/{verificationType}/send-otp")
    public ResponseEntity<String>sendVerificationOtp
            (@RequestHeader("Authorization") String jwt,
             @PathVariable VerificationType verificationType)
            throws Exception{
        User user=userService.findUserProfileByJwt(jwt);


//        YE VerificationCodeServiceImpl Mei Specially Banaya gaya hai
        VerificationCode verificationCode= verificationCodeService
                                           .getVerificationCodeById(user.getId());

      if(verificationCode ==null){
          verificationCode=verificationCodeService
                          .sendVerificationCode(user,verificationType);
      }
      if(verificationType.equals(VerificationType.EMAIL)){
          emailService.sendVerificationOtpEmail(user.getEmail(),verificationCode.getOtp());
      }
    return new ResponseEntity<>("OTP sent SUCESSFULLY",HttpStatus.OK);
    }



//    PATCH MAPPING ???
    @PatchMapping("/auth/users/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User>enableTwoFactorAuthentication(
            @PathVariable String otp,
            @RequestHeader("Authorization")String jwt) throws Exception {
    User user=userService.findUserProfileByJwt(jwt);

    VerificationCode verificationCode= verificationCodeService
                                       .getVerificationCodeByUser(user.getId());


//    THIS ONE IS IMPORTANT
    String sendTo= verificationCode
                .getVerificationType()
                .equals(VerificationType.EMAIL)
                 ? verificationCode
                     .getEmail(): verificationCode.getMobile();


    boolean isVerified=verificationCode.getOtp().equals(otp);
    if(isVerified){
        User updatedUser= userService.enableTwoFactorAuthentication(
                verificationCode.getVerificationType(),sendTo,user);

        verificationCodeService.deleteVerificationCodeById(verificationCode);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
     throw new Exception("Wrong OTP");
    }

    @PostMapping("/auth/users/reset-password/send-otp")
    public ResponseEntity<AuthResponse> sendForgotPasswordOtp(
//            WE USE AuthResponse instead of String


//            @RequestHeader("Authorization") String jwt,
//                THIS IS NOT REQUIRED:

            @RequestBody ForgotPasswordTokenRequest req)
        throws Exception{
         User user= userService.findUserProfileByJwt(jwt);
         String otp= OtpUtils.generateOTP();
         UUID uuid= UUID.randomUUID();
         String id=uuid.toString();
        ForgotPasswordToken token= forgotPasswordService.findByUser(user.getId());

        if(token==null){
            token=forgotPasswordService.createToken(user,id,
                    otp, req.getVerificationType(), req.getSendTo());
        }

        if(req.getVerificationType().equals(VerificationType.EMAIL)){
            emailService.sendVerificationOtpEmail
                    (user.getEmail(),
                    token.getOtp());
        }
        AuthResponse response= new AuthResponse();
        response.setSession(token.getId());
        response.setMessage("Password Reset OTP sent succewssfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/auth/users/reset-password/verify-otp")
        public ResponseEntity<ApiResponse>  resetPassword(
//           WE USE ApiResponse instead of String


                @RequestParam  String id,
                @RequestBody ResetPasswordRequest req,
                @RequestHeader("Authorization") String jwt) throws Exception{
        ForgotPasswordToken forgotPasswordToken= forgotPasswordService.findById(id);

        boolean isVerified= forgotPasswordToken.getOtp().equals(req.getOtp());
        if(isVerified){
            userService.updatePassword(forgotPasswordToken.getUser(), req.getPassword());
            ApiResponse res= new ApiResponse();
            res.setMessage("Password Update Successfully!");
            return new ResponseEntity<>(res,HttpStatus.ACCEPTED);
        }
        throw new Exception("Wrong OTP");
        }
}
