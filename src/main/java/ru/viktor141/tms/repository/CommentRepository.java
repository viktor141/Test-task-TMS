package ru.viktor141.tms.repository;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.viktor141.tms.model.Comment;

import java.util.Optional;

/**
 * CommentRepository provides database operations for comments.
 * <p>
 * This interface extends JpaRepository to handle CRUD operations for the Comment entity.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds all comments associated with a specific task.
     *
     * @param id       The ID of the task.
     * @param pageable The pagination settings.
     * @return A Page of Comment objects.
     */
    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId")
    Page<Comment> findAllByTaskId(@Param("taskId") Long id, Pageable pageable);

    @NonNull
    Optional<Comment> findById(@NonNull Long id);

}
