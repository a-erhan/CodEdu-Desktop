package com.codedu.services.implementations;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.ChapterRepository;
import com.codedu.repositories.interfaces.UserChapterProgressRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.UserChapterProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserChapterProgressServiceImpl implements UserChapterProgressService {

    private final UserChapterProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;

    @Autowired
    public UserChapterProgressServiceImpl(UserChapterProgressRepository progressRepository,
                                          UserRepository userRepository,
                                          ChapterRepository chapterRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.chapterRepository = chapterRepository;
    }

    @Override
    @Transactional
    public void saveProgress(UserChapterProgress progress) {
        progressRepository.update(progress);
    }

    @Override
    public double calculateCompletionRate(UserChapterProgress progress) {
        if (progress == null || progress.getChapter() == null || progress.getChapter().getTotalLessons() == 0) {
            return 0.0;
        }
        double completed = progress.getCompletedLessons();
        double total = progress.getChapter().getTotalLessons();
        return (completed / total) * 100.0;
    }

    @Override
    public UserChapterProgress getProgress(User user, Chapter chapter) {
        return progressRepository.findByUserAndChapter(user, chapter).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserChapterProgress getDetailedProgress(Long userId, Long chapterId) {
        return progressRepository.findByUserIdAndChapterIdDetailed(userId, chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter progress not found for User: " + userId));
    }

    @Override
    @Transactional
    public void saveProgressDto(ChapterProgressDTO dto, String username) {
        if (dto == null || dto.getChapter() == null || username == null) return;

        User user = userRepository.findByUsername(username).orElse(null);
        Chapter chapter = chapterRepository.findById(dto.getChapter().id()).orElse(null);

        if (user != null && chapter != null) {

            UserChapterProgress progress = progressRepository.findByUserAndChapter(user, chapter).orElse(null);

            if (progress == null) {
                progress = new UserChapterProgress();
                progress.setUser(user);
                progress.setChapter(chapter);
            }

            progress.setCompletedLessons(dto.getCompletedLessons());
            progress.setCompleted(dto.isCompleted());

            progressRepository.update(progress);
        }
    }
}