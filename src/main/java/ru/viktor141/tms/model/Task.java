package ru.viktor141.tms.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Task represents a task entity.
 * <p>
 * This entity stores information about tasks assigned to users.
 */
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * The unique identifier of the task.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the task.
     */
    @Size(min = 1, max = 1024, message = "Title must be between 1 and 1024 characters")
    private String title;

    /**
     * The description of the task.
     */
    @Size(max = 65536, message = "Description too long")
    private String description;

    /**
     * The priority level of the task.
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The status of the task.
     */
    @Enumerated(EnumType.STRING)
    private Priority priority;

    /**
     * The user who created the task.
     */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * The user to whom the task is assigned.
     */
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Comment> comments;

    /**
     * Priority represents the priority levels of a task.
     * <p>
     * This enum defines three priority levels: HIGH, MEDIUM, and LOW.
     */
    public enum Priority {
        /**
         * Indicates a high-priority task.
         */
        HIGH,

        /**
         * Indicates a medium-priority task.
         */
        MEDIUM,

        /**
         * Indicates a low-priority task.
         */
        LOW;
    }

    /**
     * Status represents the status of a task.
     * <p>
     * This enum defines three statuses: PENDING, IN_PROGRESS, and COMPLETED.
     */
    public enum Status {
        /**
         * Indicates that the task is pending (not started).
         */
        PENDING,

        /**
         * Indicates that the task is currently in progress.
         */
        IN_PROGRESS,

        /**
         * Indicates that the task has been completed.
         */
        COMPLETED;
    }
}
