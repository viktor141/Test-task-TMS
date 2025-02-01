package ru.viktor141.tms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.repository.TaskRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Page<Task> findAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ResponseEntity<Task> updateTask(Long id, Task updatedTask) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    task.setStatus(updatedTask.getStatus());
                    task.setPriority(updatedTask.getPriority());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Task> updateTaskFull(Task current, Task updatedTask) {
        // Fields to be updated
        String[] fieldsToUpdate = {"title", "description", "status", "priority", "author", "assignee"};

        try {
            for (String field : fieldsToUpdate) {
                String methodName = field.substring(0, 1).toUpperCase() + field.substring(1);

                Method getMethod = updatedTask.getClass().getMethod("get" + methodName);
                Object value = getMethod.invoke(updatedTask);

                if (value != null) {
                    Method setMethod = current.getClass().getMethod("set" + methodName, value.getClass());
                    setMethod.invoke(current, value);
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Error updating task", e);
        }

        return ResponseEntity.ok(taskRepository.save(current));
    }

    public Page<Task> findTasksByAuthorOrAssignee(Long authorId, Long assigneeId, Pageable pageable) {
        return taskRepository.findByAuthorOrAssignee(authorId, assigneeId, pageable);
    }
}
