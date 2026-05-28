package com.DeltaEdge.service;

import com.DeltaEdge.config.JwtProvider;
import com.DeltaEdge.domain.VerificationType;
import com.DeltaEdge.model.TwoFactorAuth;
import com.DeltaEdge.model.User;
import com.DeltaEdge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Fixed: Use @Service, not @RestController

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findUserProfileByJwt(String jwt) throws Exception {
        String email = JwtProvider.getEmailFromJwtToken(jwt);
        System.out.println("Extracted Email from JWT: " + email);

        // Handle the case where the repository might return Optional
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            System.err.println("User not found in H2 database for email: " + email);
            return null; // Controller will handle this and return 401
        }
        return user.get();
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        // Ensuring we unwrap the Optional correctly
        return userRepository.findByEmail(email).orElseThrow(
                () -> new Exception("User not found with email: " + email)
        );
    }

    @Override
    public User findUserById(Long userId) throws Exception {
        return userRepository.findById(userId).orElseThrow(
                () -> new Exception("User not found with ID: " + userId)
        );
    }

    @Override
    public User enableTwoFactorAuthentication(VerificationType verificationType,
                                              String sendTo,
                                              User user) {
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setSendTo(verificationType);

        user.setTwoFactorAuth(twoFactorAuth);
        return userRepository.save(user);
    }

    @Override
    public User updatePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        return userRepository.save(user);
    }
}