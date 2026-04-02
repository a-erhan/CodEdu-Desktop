package com.codedu.services.implementations;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.services.interfaces.LearningPathService;
import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.ChapterRepository;
import com.codedu.repositories.interfaces.UserChapterProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LearningPathServiceImpl implements LearningPathService {

    private final ChapterRepository chapterRepository;
    private final UserChapterProgressRepository progressRepository;

    public LearningPathServiceImpl(ChapterRepository chapterRepository,
                               UserChapterProgressRepository progressRepository) {
        this.chapterRepository = chapterRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    @Transactional // 🚀 This ensures the progress is saved to Neon correctly
    public List<ChapterProgressDTO> getOrCreateLearningPath(User user) {
        List<ChapterProgressDTO> currentPath = getLearningPathForUser(user);

        if (currentPath.isEmpty()) {
            System.out.println("Generating progress for new user in Service...");
            List<Chapter> allChapters = chapterRepository.getAll();

            for (Chapter ch : allChapters) {
                UserChapterProgress p = new UserChapterProgress();
                p.setUser(user);
                p.setChapter(ch);

                if (ch.getOrderIndex() == 1) {
                    p.setCompletedLessons(ch.getTotalLessons());
                    p.setCompleted(true);
                    p.setUnlocked(true);
                } else if (ch.getOrderIndex() == 2) {
                    p.setUnlocked(true);
                }
                progressRepository.save(p);
            }
            progressRepository.flush();
            return getLearningPathForUser(user);
        }
        return currentPath;
    }

    @Transactional(readOnly = true)
    public List<ChapterProgressDTO> getLearningPathForUser(User user) {
        // 1. Get all chapters sorted by their order index
        List<Chapter> allChapters = chapterRepository.getAll();
        allChapters.sort(Comparator.comparingInt(Chapter::getOrderIndex));

        List<ChapterProgressDTO> dtos = new ArrayList<>();

        // The first chapter is always unlocked
        boolean previousChapterCompleted = true;

        for (Chapter chapter : allChapters) {
            Optional<UserChapterProgress> progressOpt = progressRepository.findByUserAndChapter(user, chapter);

            int completed = 0;
            boolean finished = false;

            if (progressOpt.isPresent()) {
                completed = progressOpt.get().getCompletedLessons();
                finished = progressOpt.get().isCompleted();
            }

            // A chapter is locked if the previous one wasn't finished
            boolean locked = !previousChapterCompleted;

            dtos.add(ChapterProgressDTO.builder()
                    .chapter(chapter)
                    .completedLessons(completed)
                    .isCompleted(finished)
                    .isLocked(locked)
                    .build());

            // Set tracker for the next chapter in the loop
            previousChapterCompleted = finished;
        }

        return dtos;
    }
}