package com.codedu.services.interfaces;

import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;

public interface UserChapterProgressService {

    void saveProgress(UserChapterProgress progress);

    double calculateCompletionRate(UserChapterProgress progress);

    UserChapterProgress getProgress(User user, Chapter chapter);

    UserChapterProgress getDetailedProgress(Long userId, Long chapterId);

    void saveProgressDto(com.codedu.dtos.ChapterProgressDTO dto, String username);
}
