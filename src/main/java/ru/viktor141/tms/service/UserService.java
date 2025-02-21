package ru.viktor141.tms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.repository.UserRepository;
import ru.viktor141.tms.security.JwtTokenProvider;

/**
 * UserService manages user-related operations.
 * <p>
 * This service provides methods for user registration, authentication, and retrieval.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new user.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @return A JWT token for the registered user.
     */
    @Transactional
    public String registerUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.USER);

        userRepository.save(user);
        return jwtTokenProvider.generateToken(user);
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email The user's email.
     * @return True if the user exists, false otherwise.
     */
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Loads a user by their email.
     *
     * @param email The user's email.
     * @return A UserDetails object representing the user.
     * @throws UsernameNotFoundException If the user is not found.
     */
    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
