package ru.viktor141.tms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.viktor141.tms.dto.CommentDTO;
import ru.viktor141.tms.model.Comment;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.service.CommentService;

import javax.naming.NoPermissionException;

import static ru.viktor141.tms.utils.UserAuthoritiesUtils.getUserDetails;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "Endpoints for managing comments.")
public class CommentController {

    private final CommentService commentService;

    @PostMapping()
    @Operation(summary = "Add a new comment to a task", description = "Adds a new comment to the specified task.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))}),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated or don't have permission")
    })
    public ResponseEntity<Comment> addCommentToTask(@PathVariable Long taskId, @RequestBody CommentDTO commentDTO, Authentication authentication) throws NoPermissionException {
        UserDetails userDetails = getUserDetails(authentication);

        Comment comment = commentService.addCommentToTask(taskId, commentDTO, (User) userDetails);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get comments for a task", description = "Retrieves all comments for the specified task.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))}),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated")
    })
    public ResponseEntity<Page<Comment>> getCommentsByTask(
            @Parameter(description = "ID of the task") @PathVariable Long taskId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

        Pageable sortedByDateAsc = PageRequest.of(page, size, Sort.by("createdDate").ascending());
        Page<Comment> comments = commentService.getCommentsByTask(taskId, sortedByDateAsc);
        return ResponseEntity.ok(comments);
    }
}
