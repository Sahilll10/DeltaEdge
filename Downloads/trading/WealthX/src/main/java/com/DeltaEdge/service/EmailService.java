package com.DeltaEdge.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

//    WE WIL ADD JAVA MAIL DEPENDENCY
    @Autowired
    private JavaMailSender javaMailSender;


    public void sendVerificationOtpEmail(String email, String otp) {
       try{
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper
                (mimeMessage, "utf-8");
//        WHY UTF-8??

        String subjet = "Verify OTP";
        String text = "Your Verification is Successful" + otp;

        mimeMessageHelper.setSubject(subjet);
        mimeMessageHelper.setText(text);
        mimeMessageHelper.setTo(email);

//        try{
//            javaMailSender.send(mimeMessage);
//        }
    }
        catch (MailException e){
            throw new MailSendException(e.getMessage());
        }

//       ADITIONAL CATCH FOR JavaMailer
        catch (MessagingException e){
            throw new MailSendException("Mail Sending Failed"+e.getMessage());
        }
    }
}
