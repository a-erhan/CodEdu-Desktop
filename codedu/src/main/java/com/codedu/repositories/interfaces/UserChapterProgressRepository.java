package com.codedu.repositories.interfaces;

import com.codedu.models.learning.Chapter;
import com.codedu.models.learning.UserChapterProgress;
import com.codedu.models.user.User;

import java.util.List;
import java.util.Optional;

public interface UserChapterProgressRepository extends GenericRepository<UserChapterProgress> {

    // Used to check if a user has completed a specific chapter (e.g., for unlocking the next one)
    Optional<UserChapterProgress> findByUserAndChapter(User user, Chapter chapter);

    // Used to load all progress for a user when they open the Learning Path screen
    List<UserChapterProgress> findByUser(User user);
}