package ru.viktor141.tms.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.viktor141.tms.dto.CommentDTO;
import ru.viktor141.tms.model.Comment;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;
import ru.viktor141.tms.repository.CommentRepository;
import ru.viktor141.tms.repository.TaskRepository;

import java.util.Date;

/**
 * CommentService manages comment-related operations.
 * <p>
 * This service provides methods for adding and retrieving comments for tasks.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    /**
     * Retrieves comments for a specific task with pagination.
     *
     * @param taskId   The ID of the task.
     * @param pageable The pagination settings.
     * @return A Page of CommentDTO objects.
     * @throws EntityNotFoundException If the task is not found.
     */
    public Page<CommentDTO> getCommentsByTask(Long taskId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        return commentRepository.findAllByTaskId(task.getId(), pageable).map(this::convertToDTO);
    }

    /**
     * Adds a new comment to a task.
     *
     * @param taskId      The ID of the task.
     * @param commentDTO  The comment details.
     * @param user        The user adding the comment.
     * @return A CommentDTO representing the newly added comment.
     * @throws EntityNotFoundException If the task is not found.
     */
    @Transactional
    public CommentDTO addCommentToTask(Long taskId, CommentDTO commentDTO, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        comment.setAuthor(user);
        comment.setTask(task);
        comment.setCreatedDate(new Date());

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }


    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedDate(comment.getCreatedDate());
        dto.setAuthor(comment.getAuthor());
        dto.setTaskId(comment.getTask().getId());
        return dto;
    }

}
