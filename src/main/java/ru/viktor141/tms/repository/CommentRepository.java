package ru.viktor141.tms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.viktor141.tms.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByTask_Id(Long taskId, Pageable pageable);
}
