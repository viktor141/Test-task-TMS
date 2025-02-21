package ru.viktor141.tms.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.viktor141.tms.dto.TaskDTO;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.repository.TaskRepository;
import ru.viktor141.tms.utils.UserAuthoritiesUtils;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * TaskService manages task-related business logic.
 * <p>
 * This service provides methods for creating, updating, deleting, and retrieving tasks.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Retrieves all tasks with pagination.
     *
     * @param pageable The pagination settings.
     * @return A Page of TaskDTO objects.
     */
    public Page<TaskDTO> findAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param id The ID of the task.
     * @return An Optional containing the TaskDTO or empty if not found.
     */
    public Optional<TaskDTO> findTaskById(Long id) {
        return taskRepository.findById(id).map(this::convertToDTO);
    }

    /**
     * Saves a new task.
     *
     * @param task The task object to save.
     * @return A TaskDTO representing the saved task.
     */
    @Transactional
    public TaskDTO saveTask(Task task) {
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id The ID of the task to delete.
     * @return True if the task was deleted, false otherwise.
     */
    @Transactional
    public boolean deleteTask(Long id) {
        return taskRepository.findById(id).map(task -> {
            taskRepository.delete(task);
            return true;
        }).orElse(false);
    }

    /**
     * Updates an existing task.
     *
     * @param id          The ID of the task to update.
     * @param task        The updated task details.
     * @param userDetails The user details for authorization.
     * @return A ResponseEntity containing the updated task or an error message.
     */
    @Transactional
    public ResponseEntity<TaskDTO> updateTask(Long id, TaskDTO task, UserDetails userDetails) {
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty())
            return ResponseEntity.notFound().build();

        Task oldTask = optionalTask.get();

        if (UserAuthoritiesUtils.isAdmin(userDetails)) {
            return updateTaskFull(oldTask, task);
        } else if (!UserAuthoritiesUtils.isAuthorOrAssigned(convertToDTO(oldTask), userDetails)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        oldTask.setTitle(task.getTitle());
        oldTask.setDescription(task.getDescription());
        oldTask.setPriority(task.getPriority());
        oldTask.setStatus(task.getStatus());

        Task updatedTask = taskRepository.save(oldTask);
        return ResponseEntity.ok(convertToDTO(updatedTask));

    }

    /**
     * Dynamically updates all fields of a task.
     *
     * @param current     The current task object.
     * @param updatedTask The updated task details.
     * @return A ResponseEntity containing the updated task or an error message.
     */
    @Transactional
    public ResponseEntity<TaskDTO> updateTaskFull(Task current, TaskDTO updatedTask) {
        // Dynamically update all fields from TaskDTO to Task
        Field[] dtoFields = TaskDTO.class.getDeclaredFields();
        for (Field dtoField : dtoFields) {
            dtoField.setAccessible(true);

            try {
                String fieldName = dtoField.getName();
                Object fieldValue = dtoField.get(updatedTask);

                if (fieldName.equals("id"))
                    continue;

                if (fieldValue != null) {
                    Field entityField = Task.class.getDeclaredField(fieldName);
                    entityField.setAccessible(true);
                    entityField.set(current, fieldValue);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error updating field: " + dtoField.getName(), e);
            }
        }

        Task updatedTaskEntity = taskRepository.save(current);
        return ResponseEntity.ok(convertToDTO(updatedTaskEntity));
    }

    /**
     * Finds tasks based on author or assignee ID with pagination.
     *
     * @param authorId   The ID of the author (optional).
     * @param assigneeId The ID of the assignee (optional).
     * @param pageable   The pagination settings.
     * @return A Page of TaskDTO objects.
     */
    public Page<TaskDTO> findTasksByAuthorOrAssignee(Long authorId, Long assigneeId, Pageable pageable) {
        return taskRepository.findByAuthorOrAssignee(authorId, assigneeId, pageable).map(this::convertToDTO);
    }

    /**
     * Converts a Task entity to a TaskDTO.
     *
     * @param task The Task entity to convert.
     * @return A TaskDTO representing the converted task.
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setAuthor(task.getAuthor());
        dto.setAssignee(task.getAssignee());
        return dto;
    }
}
