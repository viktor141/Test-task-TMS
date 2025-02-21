package ru.viktor141.tms.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.viktor141.tms.dto.TaskDTO;
import ru.viktor141.tms.model.User;

import java.util.Optional;

/**
 * UserAuthoritiesUtils provides utility methods for user authorization checks.
 * <p>
 * This class contains methods to check if a user is an admin, author, or assignee of a task.
 */
public class UserAuthoritiesUtils {

    /**
     * Checks if the given user details represent an admin.
     *
     * @param userDetails The user details object.
     * @return True if the user is an admin, false otherwise.
     */
    public static boolean isAdmin(UserDetails userDetails) {
        return Optional.ofNullable(userDetails)
                .map(u -> u.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .orElse(Boolean.FALSE);
    }

    /**
     * Checks if the given user is the author of the task.
     *
     * @param task        The task details.
     * @param userDetails The user details object.
     * @return True if the user is the author, false otherwise.
     */
    public static boolean isAuthor(TaskDTO task, UserDetails userDetails) {
        return Optional.ofNullable(task.getAuthor())
                .map(User::getId)
                .map(authorId -> authorId.equals(((User) userDetails).getId()))
                .orElse(Boolean.FALSE);
    }

    /**
     * Checks if the given user is assigned to the task.
     *
     * @param task        The task details.
     * @param userDetails The user details object.
     * @return True if the user is assigned, false otherwise.
     */
    public static boolean isAssigned(TaskDTO task, UserDetails userDetails) {
        return Optional.ofNullable(task.getAssignee())
                .map(User::getId)
                .map(assigneeId -> assigneeId.equals(((User) userDetails).getId()))
                .orElse(Boolean.FALSE);
    }

    /**
     * Checks if the given user is the author or assignee of the task.
     *
     * @param task        The task details.
     * @param userDetails The user details object.
     * @return True if the user is the author or assignee, false otherwise.
     */
    public static boolean isAuthorOrAssigned(TaskDTO task, UserDetails userDetails) {
        return isAuthor(task, userDetails) || isAssigned(task, userDetails);
    }

    /**
     * Checks if the given user is an admin or the author/assignee of the task.
     *
     * @param task        The task details.
     * @param userDetails The user details object.
     * @return True if the user has permission, false otherwise.
     */
    public static boolean isAdminOrAuthorOrAssigned(TaskDTO task, UserDetails userDetails) {
        return isAdmin(userDetails) || isAuthorOrAssigned(task, userDetails);
    }

    /**
     * Retrieves the UserDetails object from the authentication object.
     *
     * @param authentication The authentication object.
     * @return A UserDetails object.
     */
    public static UserDetails getUserDetails(Authentication authentication) {
        return (UserDetails) authentication.getPrincipal();
    }
}
