package ru.viktor141.tms.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;

import java.util.Optional;

public class UserAuthoritiesUtils {

    public static boolean isAdmin(UserDetails userDetails) {
        return Optional.ofNullable(userDetails)
                .map(u -> u.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .orElse(false);
    }

    public static boolean isAuthor(Task task, UserDetails userDetails) {
        return Optional.ofNullable(task.getAuthor())
                .map(User::getId)
                .map(authorId -> authorId.equals(((User) userDetails).getId()))
                .orElse(false);
    }

    public static boolean isAssigned(Task task, UserDetails userDetails) {
        return Optional.ofNullable(task.getAssignee())
                .map(User::getId)
                .map(assigneeId -> assigneeId.equals(((User) userDetails).getId()))
                .orElse(false);
    }

    public static boolean isAuthorOrAssigned(Task task, UserDetails userDetails) {
        return isAuthor(task, userDetails) || isAssigned(task, userDetails);
    }

    public static boolean isAdminOrAuthorOrAssigned(Task task, UserDetails userDetails) {
        return isAdmin(userDetails) || isAuthorOrAssigned(task, userDetails);
    }

    public static UserDetails getUserDetails(Authentication authentication) {
        return (UserDetails) authentication.getPrincipal();
    }
}
