package ru.viktor141.tms.service;

import jakarta.persistence.EntityNotFoundException;
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

import javax.naming.NoPermissionException;

import static ru.viktor141.tms.utils.UserAuthoritiesUtils.isAdminOrAuthorOrAssigned;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public Page<Comment> getCommentsByTask(Long taskId, Pageable pageable) {
        return commentRepository.findAllByTask_Id(taskId, pageable);
    }

    public Comment addCommentToTask(Long taskId, CommentDTO commentDTO, User user) throws NoPermissionException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if(!isAdminOrAuthorOrAssigned(task, user))
            throw new NoPermissionException("You don't have permission");


        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        comment.setAuthor(user);
        comment.setTask(task);

        return commentRepository.save(comment);
    }

}
