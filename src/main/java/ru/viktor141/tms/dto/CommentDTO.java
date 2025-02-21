package ru.viktor141.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.viktor141.tms.model.User;

import java.util.Date;

/**
 * CommentDTO represents the data transfer object for a comment.
 * <p>
 * This class is used to transfer comment data between the client and server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    /**
     * The unique identifier of the comment.
     */
    private Long id;
    /**
     * The text content of the comment.
     */
    private String text;
    /**
     * The creation date of the comment.
     */
    private Date createdDate;
    /**
     * The user who authored the comment.
     */
    private User author;
    /**
     * The ID of the task to which the comment is associated.
     */
    private Long taskId;
}
