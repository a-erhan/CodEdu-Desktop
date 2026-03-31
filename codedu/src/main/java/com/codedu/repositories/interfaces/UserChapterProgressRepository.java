package com.codedu.repositories.interfaces;

import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;
import java.util.List;
import java.util.Optional;

public interface UserChapterProgressRepository extends GenericRepository<UserChapterProgress> {

    // Add these so the Implementation can @Override them
    Optional<UserChapterProgress> findByUserAndChapter(User user, Chapter chapter);

    List<UserChapterProgress> findByUser(User user);

    Optional<UserChapterProgress> findByUserIdAndChapterIdDetailed(Long userId, Long chapterId);
}