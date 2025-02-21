package ru.viktor141.tms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.viktor141.tms.dto.CommentDTO;
import ru.viktor141.tms.dto.TaskDTO;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.service.CommentService;
import ru.viktor141.tms.service.TaskService;
import ru.viktor141.tms.utils.PageUtils;
import ru.viktor141.tms.utils.UserAuthoritiesUtils;

import java.util.Optional;

/**
 * CommentController manages comment-related endpoints.
 * <p>
 * This controller provides RESTful APIs for adding and retrieving comments for tasks.
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "Endpoints for managing comments.")
public class CommentController {

    private final CommentService commentService;
    private final TaskService taskService;

    /**
     * Adds a new comment to a specific task.
     *
     * @param taskId        The ID of the task to which the comment will be added.
     * @param commentDTO    The comment object containing the text to be saved.
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity containing the created comment or an error message.
     */
    @PostMapping
    @Operation(summary = "Add a new comment to a task",
            description = "Adds a new comment to the specified task.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comment created successfully", content = @Content(schema = @Schema(implementation = CommentDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to add a comment"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<CommentDTO> addCommentToTask(
            @Parameter(description = "ID of the task") @PathVariable Long taskId,
            @Valid @RequestBody @Parameter(description = "Comment object containing text") CommentDTO commentDTO,
            Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);
        Optional<TaskDTO> task = taskService.findTaskById(taskId);

        if (task.isEmpty()) {
            throw new EntityNotFoundException("Task not found with id: " + taskId);
        }

        if (!UserAuthoritiesUtils.isAdminOrAuthorOrAssigned(task.get(), userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CommentDTO newCommentDTO = commentService.addCommentToTask(taskId, commentDTO, (User) userDetails);
        return new ResponseEntity<>(newCommentDTO, HttpStatus.CREATED);
    }

    /**
     * Retrieves all comments for a specific task with pagination.
     *
     * @param taskId  The ID of the task for which comments are retrieved.
     * @param page    The page number (default: 0).
     * @param size    The page size (default: 10).
     * @return A ResponseEntity containing a page of comments or an error message.
     */
    @GetMapping
    @Operation(summary = "Get comments for a task",
            description = "Retrieves all comments for the specified task with pagination.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<Page<CommentDTO>> getCommentsByTask(
            @Parameter(description = "ID of the task") @PathVariable Long taskId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageUtils.createPageable(page, size, new String[]{"createdDate", "asc"});
        Page<CommentDTO> comments = commentService.getCommentsByTask(taskId, pageable);
        return ResponseEntity.ok(comments);
    }
}
