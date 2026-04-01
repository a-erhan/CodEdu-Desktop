package com.codedu.services.implementations;

import com.codedu.models.learning.Chapter;
import com.codedu.services.interfaces.UserChapterProgressService;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.UserChapterProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserChapterProgressServiceImpl implements UserChapterProgressService {

    private final UserChapterProgressRepository progressRepository;

    @Autowired
    public UserChapterProgressServiceImpl(UserChapterProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    // Save progress to the database
    @Transactional
    public void saveProgress(UserChapterProgress progress) {
        progressRepository.update(progress);
    }

    // STEP 5: Calculate the completion percentage of a chapter
    public double calculateCompletionRate(UserChapterProgress progress) {
        if (progress == null || progress.getChapter() == null || progress.getChapter().getTotalLessons() == 0) {
            return 0.0;
        }

        double completed = progress.getCompletedLessons();
        double total = progress.getChapter().getTotalLessons();

        return (completed / total) * 100.0;
    }

    /**
     * Fetches the progress record for a specific user and chapter.
     */
    public UserChapterProgress getProgress(User user, Chapter chapter) {
        // We call the repository method we created earlier
        return progressRepository.findByUserAndChapter(user, chapter).orElse(null);
    }

    @Override
    @Transactional
    public UserChapterProgress getOrCreateProgress(User user, Chapter chapter) {
        return progressRepository.findByUserAndChapter(user, chapter)
                .orElseGet(() -> {
                    UserChapterProgress newProgress = UserChapterProgress.builder()
                            .user(user)
                            .chapter(chapter)
                            .completedLessons(0)
                            .isCompleted(false)
                            .isUnlocked(true)
                            .build();
                    progressRepository.save(newProgress);
                    return newProgress;
                });
    }
}