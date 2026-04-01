package com.codedu.services.interfaces;

import com.codedu.dtos.ChapterProgressDTO;
import com.codedu.models.user.User;

import java.util.List;

public interface LearningPathService {

    List<ChapterProgressDTO> getLearningPathForUser(User user);
}
