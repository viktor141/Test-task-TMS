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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.viktor141.tms.dto.TaskDTO;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.service.TaskService;
import ru.viktor141.tms.utils.PageUtils;
import ru.viktor141.tms.utils.UserAuthoritiesUtils;

import java.util.Arrays;
import java.util.Optional;


/**
 * TaskController manages task-related endpoints.
 * <p>
 * This controller provides RESTful APIs for creating, retrieving, updating, and deleting tasks.
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing tasks.")
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new task.
     *
     * @param task           The task object containing details to be saved.
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity containing the created task or an error message.
     */
    @PostMapping("/create")
    @Operation(summary = "Create a new task",
            description = "Creates a new task and assigns the author if not specified.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid input"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin and did not specify an author"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody Task task, Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);
        if (task.getAuthor() == null) {
            task.setAuthor((User) userDetails);
        }
        TaskDTO newTaskDTO = taskService.saveTask(task);
        return new ResponseEntity<>(newTaskDTO, HttpStatus.CREATED);
    }

    /**
     * Retrieves all tasks based on user role (admin or regular user).
     *
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity containing a list of tasks or an error message.
     */
    @GetMapping("/all")
    @Operation(summary = "Get all tasks",
            description = "Returns all tasks. Admins can see all tasks, while regular users can only see their own tasks.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to view all tasks"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<Page<TaskDTO>> getAllTasks(Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);
        if (UserAuthoritiesUtils.isAdmin(userDetails)) {
            return ResponseEntity.ok(taskService.findAllTasks(Pageable.unpaged()));
        }
        long id = ((User) userDetails).getId();
        Page<TaskDTO> tasks = taskService.findTasksByAuthorOrAssignee(id, id, Pageable.unpaged());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves tasks based on author or assignee ID with pagination and sorting options.
     *
     * @param authorId       The ID of the author (optional).
     * @param assigneeId     The ID of the assignee (optional).
     * @param page           The page number (default: 0).
     * @param size           The page size (default: 10).
     * @param sort           The sort parameters (default: id,desc).
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity containing a page of tasks or an error message.
     */
    @GetMapping
    @Operation(summary = "Get tasks by author or assignee",
            description = "Retrieves tasks based on author or assignee ID with pagination and sorting options.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to view requested tasks"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<Page<TaskDTO>> getTasksByAuthorOrAssignee(
            @Parameter(description = "ID of the author") @RequestParam(required = false) Long authorId,
            @Parameter(description = "ID of the assignee") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort parameters") @RequestParam(defaultValue = "id,desc") String[] sort,
            Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);
        long id = ((User) userDetails).getId();
        log.info(Arrays.toString(sort));
        if (!UserAuthoritiesUtils.isAdmin(userDetails)) {
            if (authorId != null && authorId != id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (assigneeId != null && assigneeId != id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        Pageable pageable = PageUtils.createPageable(page, size, sort);
        Page<TaskDTO> tasks = taskService.findTasksByAuthorOrAssignee(authorId, assigneeId, pageable);
        return ResponseEntity.ok(tasks);
    }


    /**
     * Retrieves a specific task by its ID.
     *
     * @param id             The ID of the task to retrieve.
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity containing the task or an error message.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID",
            description = "Retrieves a specific task by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task retrieved successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to view the task"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);
        Optional<TaskDTO> task = taskService.findTaskById(id);

        if (task.isEmpty()) {
            throw new EntityNotFoundException("Task not found");
        }

        if (!UserAuthoritiesUtils.isAdminOrAuthorOrAssigned(task.get(), userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(task.get());
    }

    /**
     * Updates an existing task.
     *
     * @param id             The ID of the task to update.
     * @param task           The updated task details.
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity containing the updated task or an error message.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a task",
            description = "Updates an existing task. Only admins or the author/assignee can update a task.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to update the task"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO task, Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);

        return taskService.updateTask(id, task, userDetails);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id             The ID of the task to delete.
     * @param authentication The authentication object for user details.
     * @return A ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task",
            description = "Deletes a task by its ID. Only admins or the author can delete a task.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to delete the task"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            })
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = UserAuthoritiesUtils.getUserDetails(authentication);
        Optional<TaskDTO> task = taskService.findTaskById(id);

        if (task.isEmpty()) {
            throw new EntityNotFoundException("Task not found");
        }

        if (!UserAuthoritiesUtils.isAdmin(userDetails) && !UserAuthoritiesUtils.isAuthor(task.get(), userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (taskService.deleteTask(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
