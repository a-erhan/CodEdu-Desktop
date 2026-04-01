package com.codedu.repositories.interfaces;

import com.codedu.models.learning.Chapter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends GenericRepository<Chapter> {
    List<Chapter> findByDifficulty(Chapter.Difficulty difficulty);
    List<Chapter> findByTopicName(String topicName);
    List<Chapter> findAll();

    @Query("SELECT c FROM Chapter c LEFT JOIN FETCH c.content cc LEFT JOIN FETCH cc.questions WHERE c.id = :id")
    Optional<Chapter> findByIdWithQuestions(@Param("id") Long id);
}
