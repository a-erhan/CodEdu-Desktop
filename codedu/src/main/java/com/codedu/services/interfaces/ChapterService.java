package com.codedu.services.interfaces;

import com.codedu.models.learning.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterService {

    Optional<Chapter> getChapterById(int id);

    List<Chapter> getAllChapters();


    Optional<Chapter> getChapterWithQuestions(Long id);
}
