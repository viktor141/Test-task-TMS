package ru.viktor141.tms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktor141.tms.model.Task;

/**
 * TaskRepository provides database operations for tasks.
 * <p>
 * This interface extends JpaRepository to handle CRUD operations for the Task entity.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds all tasks matching the given specification with pagination.
     *
     * @param spec      The specification for filtering tasks.
     * @param pageable  The pagination settings.
     * @return A Page of Task objects.
     */
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    /**
     * Creates a specification for filtering tasks by author ID.
     *
     * @param authorId The ID of the author.
     * @return A Specification object.
     */
    static Specification<Task> hasAuthor(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    /**
     * Creates a specification for filtering tasks by assignee ID.
     *
     * @param assigneeId The ID of the assignee.
     * @return A Specification object.
     */
    static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    /**
     * Finds tasks based on author or assignee ID with pagination.
     *
     * @param authorId   The ID of the author (optional).
     * @param assigneeId The ID of the assignee (optional).
     * @param pageable   The pagination settings.
     * @return A Page of Task objects.
     */
    default Page<Task> findByAuthorOrAssignee(Long authorId, Long assigneeId, Pageable pageable) {
        Specification<Task> spec = Specification.where(null);

        if (authorId != null && assigneeId != null) {
            spec = Specification.where(hasAuthor(authorId)).or(hasAssignee(assigneeId));
        } else {
            if (authorId != null) {
                spec = spec.and(hasAuthor(authorId));
            }
            if (assigneeId != null) {
                spec = spec.and(hasAssignee(assigneeId));
            }
        }

        return findAll(spec, pageable);
    }
}
