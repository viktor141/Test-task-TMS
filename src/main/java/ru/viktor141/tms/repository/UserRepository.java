package ru.viktor141.tms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktor141.tms.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
