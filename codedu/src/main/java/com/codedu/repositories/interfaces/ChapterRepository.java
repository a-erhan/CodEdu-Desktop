package com.codedu.repositories.interfaces;

import com.codedu.models.Chapter;

import java.util.List;

public interface ChapterRepository extends GenericRepository<Chapter> {
    List<Chapter> findByDifficulty(Chapter.Difficulty difficulty);
    List<Chapter> findByTopicName(String topicName);
}
