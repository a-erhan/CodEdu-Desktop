package com.codedu.services.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.repositories.interfaces.ChapterRepository;
import com.codedu.services.interfaces.ChapterSyncService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ChapterSyncServiceImpl implements ChapterSyncService {

    @Autowired
    private ChapterRepository chapterRepository;

    private static final Map<String, String> TITLE_TO_ICON = Map.ofEntries(
            Map.entry("Hello, World", "/com/codedu/images/learning-path/ch_hello_world.png"),
            Map.entry("Variables",    "/com/codedu/images/learning-path/ch_variables.png"),
            Map.entry("Control Flow", "/com/codedu/images/learning-path/ch_control_flow.png"),
            Map.entry("Operators",    "/com/codedu/images/learning-path/ch_operators.png"),
            Map.entry("Loops",        "/com/codedu/images/learning-path/ch_loops.png"),
            Map.entry("Methods",      "/com/codedu/images/learning-path/ch_functions.png"),
            Map.entry("Arrays",       "/com/codedu/images/learning-path/ch_arrays.png"),
            Map.entry("OOP",          "/com/codedu/images/learning-path/ch_oop.png"),
            Map.entry("Inheritance",  "/com/codedu/images/learning-path/ch_inheritance.png"),
            Map.entry("Exceptions",   "/com/codedu/images/learning-path/ch_exceptions.png"),
            Map.entry("Lists",        "/com/codedu/images/learning-path/ch_linear_ds.png"),
            Map.entry("Trees",        "/com/codedu/images/learning-path/ch_nonlinear_ds.png")
    );

    @Override
    @PostConstruct
    @Transactional
    public void syncChapterLessonCounts() {
        try {
            List<Chapter> chapters = chapterRepository.findAll();
            int updatedCount = 0;

            for (Chapter chapter : chapters) {
                boolean dirty = false;

                if (chapter.getContent() != null && chapter.getContent().getQuestions() != null) {
                    int realQuestionCount = chapter.getContent().getQuestions().size();
                    if (chapter.getTotalLessons() != realQuestionCount) {
                        chapter.setTotalLessons(realQuestionCount);
                        dirty = true;
                    }
                }

                if ((chapter.getIconImage() == null || chapter.getIconImage().isBlank())
                        && chapter.getTitle() != null) {
                    for (Map.Entry<String, String> e : TITLE_TO_ICON.entrySet()) {
                        if (chapter.getTitle().contains(e.getKey())) {
                            chapter.setIconImage(e.getValue());
                            dirty = true;
                            break;
                        }
                    }
                }

                if (dirty) {
                    chapterRepository.save(chapter);
                    updatedCount++;
                }
            }

            if (updatedCount > 0) {
                System.out.println("✅ Synced " + updatedCount + " chapters (lesson counts + icons).");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not sync chapters: " + e.getMessage());
        }
    }
}