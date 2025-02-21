package ru.viktor141.tms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserDTO represents the data transfer object for a user.
 * <p>
 * This class is used to transfer user data between the client and server.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    /**
     * The email address of the user.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * The password of the user.
     */
    @NotBlank
    private String password;

    /**
     * The role of the user.
     */
    private String role;
}
