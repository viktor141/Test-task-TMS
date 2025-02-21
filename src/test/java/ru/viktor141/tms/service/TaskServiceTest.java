package ru.viktor141.tms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.viktor141.tms.dto.TaskDTO;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.repository.TaskRepository;
import ru.viktor141.tms.utils.UserAuthoritiesUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserAuthoritiesUtils userAuthoritiesUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllTasks() {
        // Arrange
        Page<Task> mockPage = new PageImpl<>(List.of(new Task(), new Task()));
        when(taskRepository.findAll((Pageable) any())).thenReturn(mockPage);

        // Act
        Page<TaskDTO> result = taskService.findAllTasks(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(taskRepository, times(1)).findAll((Pageable) any());
    }

    @Test
    void testFindTaskByIdExistingTask() {
        // Arrange
        Long taskId = 1L;
        Task mockTask = new Task();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        // Act
        Optional<TaskDTO> result = taskService.findTaskById(taskId);

        // Assert
        assertTrue(result.isPresent());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void testSaveTask() {
        // Arrange
        Task task = new Task();
        when(taskRepository.save(task)).thenReturn(task);

        // Act
        TaskDTO result = taskService.saveTask(task);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testDeleteTaskSuccess() {
        // Arrange
        Long taskId = 1L;
        Task mockTask = new Task();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        // Act
        boolean result = taskService.deleteTask(taskId);

        // Assert
        assertTrue(result);
        verify(taskRepository, times(1)).delete(mockTask);
    }

    @Test
    void testUpdateTaskAuthorizedUser() {
        // Arrange
        Long taskId = 1L;
        User user = new User();

        user.setId(1L);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setAuthor(user);

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Title");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        // Act
        ResponseEntity<TaskDTO> response = taskService.updateTask(taskId, updatedTaskDTO, user);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        TaskDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateTaskFull() {
        // Arrange
        Long taskId = 1L;

        Task currentTask = new Task();
        currentTask.setId(taskId);
        currentTask.setTitle("Old Title");

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("New Title");

        when(taskRepository.save(currentTask)).thenReturn(currentTask);

        // Act
        ResponseEntity<TaskDTO> response = taskService.updateTaskFull(currentTask, updatedTaskDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        TaskDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());

        verify(taskRepository, times(1)).save(any(Task.class));
    }
}
