package com.codedu.services.interfaces;

import com.codedu.dtos.learning.ChapterDTO;
import com.codedu.models.learning.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterService {

    Optional<ChapterDTO> getChapterById(int id);

    List<ChapterDTO> getAllChapters();

    Optional<Chapter> getChapterWithQuestions(Long id);
}
