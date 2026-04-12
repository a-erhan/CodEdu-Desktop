package com.codedu.services.implementations;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.dtos.learning.ChapterDTO;
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
    @Transactional
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

            currentPath = getLearningPathForUser(user);
        }

        return currentPath;
    }

    @Transactional(readOnly = true)
    public List<ChapterProgressDTO> getLearningPathForUser(User user) {

        List<Chapter> allChapters = chapterRepository.getAll();
        allChapters.sort(Comparator.comparingInt(Chapter::getOrderIndex));

        List<ChapterProgressDTO> dtos = new ArrayList<>();

        boolean previousChapterCompleted = true;

        for (Chapter chapter : allChapters) {
            Optional<UserChapterProgress> progressOpt = progressRepository.findByUserAndChapter(user, chapter);

            int completed = 0;
            boolean finished = false;

            if (progressOpt.isPresent()) {
                completed = progressOpt.get().getCompletedLessons();
                finished = progressOpt.get().isCompleted();
            }

            boolean locked = !previousChapterCompleted;

            if (user.isTesterAccount()) {
                locked = false;
            }

            int dynamicLessons = 0;
            if (chapter.getContent() != null && chapter.getContent().getQuestions() != null) {
                dynamicLessons = chapter.getContent().getQuestions().size();
            }

            ChapterDTO chapterDTO = ChapterDTO.builder()
                    .id(chapter.getId())
                    .title(chapter.getTitle())
                    .description(chapter.getDescription())
                    .iconEmoji(chapter.getIconEmoji())
                    .iconImage(chapter.getIconImage())
                    .difficulty(chapter.getDifficulty())
                    .totalLessons(chapter.getTotalLessons())
                    .xpReward(chapter.getXpReward())
                    .tokenReward(chapter.getTokenReward())
                    .orderIndex(chapter.getOrderIndex())
                    .topicName(chapter.getTopicName())
                    .build();

            dtos.add(ChapterProgressDTO.builder()
                    .chapter(chapterDTO)
                    .completedLessons(completed)
                    .isCompleted(finished)
                    .isLocked(locked)
                    .dynamicTotalLessons(dynamicLessons)
                    .build());

            previousChapterCompleted = finished;
        }

        return dtos;
    }
}