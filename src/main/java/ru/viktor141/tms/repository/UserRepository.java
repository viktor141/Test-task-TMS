package ru.viktor141.tms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktor141.tms.model.User;

import java.util.Optional;

/**
 * UserRepository provides database operations for users.
 * <p>
 * This interface extends JpaRepository to handle CRUD operations for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their email address.
     *
     * @param email The email address of the user.
     * @return An Optional containing the User object or empty if not found.
     */
    Optional<User> findByEmail(String email);
}
