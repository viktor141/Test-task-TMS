package ru.viktor141.tms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.service.TaskService;
import ru.viktor141.tms.utils.PageUtils;

import java.util.Optional;

import static ru.viktor141.tms.utils.UserAuthoritiesUtils.*;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing tasks.")
public class TaskController {

    private final TaskService taskService;


    @PostMapping("/create")
    @Operation(summary = "Create a new task",
            description = "Creates a new task and assigns the author if not specified.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = Task.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated")
            })
    public ResponseEntity<Task> createTask(@RequestBody Task task, Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        if (!isAdmin(userDetails) || task.getAuthor() == null) {
            task.setAuthor((User) userDetails);
        }
        Task newTask = taskService.saveTask(task);
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }


    @GetMapping("/all")
    @Operation(summary = "Get all tasks",
            description = "Returns all tasks. Admins can see all tasks, while regular users can only see their own tasks.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated")
            })
    public ResponseEntity<Page<Task>> getAllTasks(Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        if (isAdmin(userDetails))
            return ResponseEntity.ok(taskService.findAllTasks(Pageable.unpaged()));

        long id = ((User) userDetails).getId();
        Page<Task> tasks = taskService.findTasksByAuthorOrAssignee(id, id, Pageable.unpaged());
        return ResponseEntity.ok(tasks);
    }


    @GetMapping
    @Operation(summary = "Get tasks by author or assignee",
            description = "Retrieves tasks based on author or assignee ID with pagination and sorting options.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated or isn't author/assigned")
            })
    public ResponseEntity<Page<Task>> getTasksByAuthorOrAssignee(
            @Parameter(description = "ID of the author") @RequestParam(required = false) Long authorId,
            @Parameter(description = "ID of the assignee") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort parameters") @RequestParam(defaultValue = "id,desc") String[] sort,
            Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        long userId = ((User) userDetails).getId();

        if (!isAdmin(userDetails))
            if (authorId != null && authorId != userId
                    || assigneeId != null && assigneeId != userId)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Pageable pageable = PageUtils.createPageable(page, size, sort);
        Page<Task> tasks = taskService.findTasksByAuthorOrAssignee(authorId, assigneeId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID",
            description = "Retrieves a specific task by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task retrieved successfully", content = @Content(schema = @Schema(implementation = Task.class))),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated or isn't author/assigned")
            })
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        Optional<Task> task = taskService.findTaskById(id);

        if (task.isEmpty()) {
            throw new EntityNotFoundException("Task not found");
        }

        if (!isAdminOrAuthorOrAssigned(task.get(), userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(task.get());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task",
            description = "Updates an existing task. Only admins or the author/assignee can update a task.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = Task.class))),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated or isn't author/assigned")
            })
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task, Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        Optional<Task> oldTask = taskService.findTaskById(id);
        if (oldTask.isEmpty())
            throw new EntityNotFoundException("Task not found");
        if (isAdmin(userDetails))
            return taskService.updateTaskFull(oldTask.get(), task);
        if (!isAuthorOrAssigned(oldTask.get(), userDetails))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task",
            description = "Deletes a task by its ID. Only admins or the author can delete a task.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Task not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User isn't authenticated or isn't author")
            })
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        Optional<Task> task = taskService.findTaskById(id);
        if (task.isEmpty())
            throw new EntityNotFoundException("Task not found");
        if (!isAdmin(userDetails) && !isAuthor(task.get(), userDetails))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (taskService.deleteTask(id))
            return ResponseEntity.ok().build();
        return ResponseEntity.internalServerError().build();
    }
}
