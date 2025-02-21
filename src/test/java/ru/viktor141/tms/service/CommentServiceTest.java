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
import ru.viktor141.tms.dto.CommentDTO;
import ru.viktor141.tms.model.Comment;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.repository.CommentRepository;
import ru.viktor141.tms.repository.TaskRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddCommentToTask() {
        // Arrange
        Long taskId = 1L;
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText("Test Comment");

        User mockUser = new User();
        mockUser.setId(1L);

        Task mockTask = new Task();
        mockTask.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        Comment mockComment = new Comment();
        mockComment.setId(1L);
        mockComment.setText("Test Comment");
        mockComment.setAuthor(mockUser);
        mockComment.setTask(mockTask);
        mockComment.setCreatedDate(new Date());

        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        // Act
        CommentDTO result = commentService.addCommentToTask(taskId, commentDTO, mockUser);

        // Assert
        assertNotNull(result);
        assertEquals("Test Comment", result.getText());
        assertNotNull(result.getCreatedDate());
        assertNotNull(result.getAuthor());
        assertNotNull(result.getTaskId());
        verify(taskRepository, times(1)).findById(taskId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testGetCommentsByTaskEmptyList() {
        // Arrange
        Long taskId = 1L;

        // Create a mock Task
        Task mockTask = new Task();
        mockTask.setId(taskId);

        // Mock the repository behavior
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        Page<Comment> mockEmptyPage = new PageImpl<>(Collections.emptyList());
        when(commentRepository.findAllByTaskId(eq(taskId), any(Pageable.class))).thenReturn(mockEmptyPage);

        // Act
        Page<CommentDTO> result = commentService.getCommentsByTask(taskId, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());

        verify(taskRepository, times(1)).findById(taskId);
        verify(commentRepository, times(1)).findAllByTaskId(eq(taskId), any(Pageable.class));
    }

    @Test
    void testGetCommentsByTask() {
        // Arrange
        Long taskId = 1L;

        // Create a mock Task
        Task mockTask = new Task();
        mockTask.setId(taskId);

        // Mock the repository behavior
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setText("Comment 1");
        comment1.setCreatedDate(new Date());
        comment1.setAuthor(new User());
        comment1.setTask(mockTask);

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setText("Comment 2");
        comment2.setCreatedDate(new Date());
        comment2.setAuthor(new User());
        comment2.setTask(mockTask);

        Page<Comment> mockPage = new PageImpl<>(List.of(comment1, comment2));
        when(commentRepository.findAllByTaskId(eq(taskId), any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<CommentDTO> result = commentService.getCommentsByTask(taskId, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        CommentDTO dto1 = result.getContent().get(0);
        CommentDTO dto2 = result.getContent().get(1);

        assertNotNull(dto1);
        assertNotNull(dto2);

        assertEquals("Comment 1", dto1.getText());
        assertEquals("Comment 2", dto2.getText());

        verify(taskRepository, times(1)).findById(taskId);
        verify(commentRepository, times(1)).findAllByTaskId(eq(taskId), any(Pageable.class));
    }


}
