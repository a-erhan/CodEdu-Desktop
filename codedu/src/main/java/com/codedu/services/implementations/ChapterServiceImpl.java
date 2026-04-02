package com.codedu.services.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.services.interfaces.ChapterService;
import com.codedu.repositories.interfaces.ChapterRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;

    @Autowired
    public ChapterServiceImpl(ChapterRepository chapterRepository) {
        this.chapterRepository = chapterRepository;
    }

    public Optional<Chapter> getChapterById(int id) {
        return chapterRepository.findById(id);
    }

    public List<Chapter> getAllChapters() {
        return chapterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Chapter> getChapterWithQuestions(Long id) {
        // This calls the FETCH JOIN query we created in Step 1
        return chapterRepository.findByIdWithQuestions(id);
    }
}