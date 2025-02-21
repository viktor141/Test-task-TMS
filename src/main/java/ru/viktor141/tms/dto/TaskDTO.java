package ru.viktor141.tms.dto;

import lombok.*;
import ru.viktor141.tms.model.Task;
import ru.viktor141.tms.model.User;

/**
 * TaskDTO represents the data transfer object for a task.
 * <p>
 * This class is used to transfer task data between the client and server.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {

    /**
     * The unique identifier of the task.
     */
    private Long id;

    /**
     * The title of the task.
     */
    private String title;

    /**
     * The title of the task.
     */
    private String description;

    /**
     * The priority level of the task.
     */
    private Task.Priority priority;

    /**
     * The status of the task.
     */
    private Task.Status status;

    /**
     * The user who created the task.
     */
    private User author;

    /**
     * The user to whom the task is assigned.
     */
    private User assignee;
}
