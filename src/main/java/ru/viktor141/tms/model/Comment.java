package ru.viktor141.tms.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Comment represents a comment associated with a task.
 * <p>
 * This entity stores information about comments made by users on tasks.
 */
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    /**
     * The unique identifier of the comment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The text content of the comment.
     */
    @NotNull
    @Size(max = 65536, message = "Comment too long")
    private String text;

    /**
     * The user who authored the comment.
     */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * The task to which the comment is associated.
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * The creation date of the comment.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
}
