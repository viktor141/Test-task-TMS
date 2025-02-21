package ru.viktor141.tms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.repository.UserRepository;
import ru.viktor141.tms.security.JwtTokenProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);

        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtTokenProvider.generateToken(any())).thenReturn("mockedJWTToken");

        // Act
        String token = userService.registerUser(email, password);

        // Assert
        assertNotNull(token);
        assertEquals("mockedJWTToken", token);
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtTokenProvider, times(1)).generateToken(any());
    }

    @Test
    void testEmailExistsTrue() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // Act
        boolean result = userService.emailExists(email);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testEmailExistsFalse() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.emailExists(email);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername() {
        // Arrange
        String email = "test@example.com";
        User mockUser = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.loadUserByUsername(email);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findByEmail(email);
    }
}
