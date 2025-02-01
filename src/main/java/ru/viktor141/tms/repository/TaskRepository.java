package ru.viktor141.tms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktor141.tms.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    static Specification<Task> hasAuthor(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

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
