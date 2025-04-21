package com.notiq.app.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notiq.app.Model.Note;

public interface NoteRepo extends JpaRepository<Note, Integer> {
    List<Note> findByUserIdAndIsFavoriteTrue(Integer userId);

    List<Note> findByUserIdAndIsArchivedTrue(Integer userId);

    List<Note> findByUserIdAndIsTrashedTrue(Integer userId);

    List<Note> findByUserIdAndIsArchivedFalseAndIsTrashedFalse(Integer id);

    List<Note> findTop6ByUserIdAndIsArchivedFalseAndIsTrashedFalseOrderByCreatedAtDesc(Integer id);

    long countByUserId(Integer userId);

    long countByUserIdAndIsFavoriteTrue(Integer userId);

    long countByUserIdAndIsArchivedTrue(Integer userId);

    long countByUserIdAndIsTrashedTrue(Integer userId);
}
