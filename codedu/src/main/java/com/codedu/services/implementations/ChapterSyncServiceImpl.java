package com.codedu.services.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.repositories.interfaces.ChapterRepository;
import com.codedu.services.interfaces.ChapterSyncService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChapterSyncServiceImpl implements ChapterSyncService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Override
    @PostConstruct
    @Transactional
    public void syncChapterLessonCounts() {
        try {
            List<Chapter> chapters = chapterRepository.findAll();
            int updatedCount = 0;

            for (Chapter chapter : chapters) {
                if (chapter.getContent() != null && chapter.getContent().getQuestions() != null) {
                    int realQuestionCount = chapter.getContent().getQuestions().size();

                    // If the database number is wrong, fix it!
                    if (chapter.getTotalLessons() != realQuestionCount) {
                        chapter.setTotalLessons(realQuestionCount);
                        chapterRepository.save(chapter);
                        updatedCount++;
                    }
                }
            }

            if (updatedCount > 0) {
                System.out.println("✅ Synced " + updatedCount + " chapters with their true lesson counts!");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not sync chapter lesson counts: " + e.getMessage());
        }
    }
}