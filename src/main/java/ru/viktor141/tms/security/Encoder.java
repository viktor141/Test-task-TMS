package ru.viktor141.tms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Encoder configures password encoding for the application.
 * <p>
 * This class provides a PasswordEncoder bean for hashing passwords.
 */
@Component
public class Encoder {

    /**
     * Provides a BCryptPasswordEncoder bean for password encoding.
     *
     * @return A PasswordEncoder object.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
