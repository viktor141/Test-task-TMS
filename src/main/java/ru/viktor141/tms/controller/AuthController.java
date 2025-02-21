package ru.viktor141.tms.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.viktor141.tms.dto.UserDTO;
import ru.viktor141.tms.security.JwtTokenProvider;
import ru.viktor141.tms.service.UserService;

/**
 * AuthController manages authentication and registration endpoints.
 * <p>
 * This controller provides APIs for user registration and login.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Endpoints for authentication and registration.")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user.
     *
     * @param userDTO The user object containing email and password.
     * @return A ResponseEntity containing the JWT token or an error message.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Registers a new user with the provided email and password.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request - Email already exists or invalid input"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<String> registerUser(@Valid @RequestBody @Parameter(description = "User object containing email and password") UserDTO userDTO) {
        if (userService.emailExists(userDTO.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", userDTO.getEmail());
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
        String token = userService.registerUser(userDTO.getEmail(), userDTO.getPassword());
        log.info("User registered successfully - {}", userDTO.getEmail());
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param userDTO The user object containing email and password.
     * @return A ResponseEntity containing the JWT token or an error message.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate a user",
            description = "Authenticates a user with the provided email and password and returns a JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User logged in successfully",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid username or password"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<String> loginUser(@Valid @RequestBody @Parameter(description = "User object containing email and password") UserDTO userDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);
            log.info("User logged in successfully - {}", userDTO.getEmail());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.warn("Login failed: Invalid username or password - {}", userDTO.getEmail());
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
