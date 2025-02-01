package ru.viktor141.tms.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.security.JwtTokenProvider;
import ru.viktor141.tms.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Endpoints for authentication and registration.")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with the provided email and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Email already exists", content = @Content)
    })
    public ResponseEntity<String> registerUser(@Valid @RequestBody @Parameter(description = "User object containing email and password") User user) {
        if (userService.emailExists(user.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", user.getEmail());
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
        String token = userService.registerUser(user.getEmail(), user.getPassword());
        log.info("User registered successfully - {}", user.getEmail());
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    @Operation(summary = "Authenticate a user", description = "Authenticates a user with the provided email and password and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content)
    })
    public ResponseEntity<String> loginUser(@Valid @RequestBody @Parameter(description = "User object containing email and password") User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);
            log.info("User logged in successfully - {}", user.getEmail());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.warn("Login failed: Invalid username or password - {}", user.getEmail());
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
