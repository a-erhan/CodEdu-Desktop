package com.codedu.repositories.implementations;

import com.codedu.models.learning.LearningPath;
import com.codedu.repositories.interfaces.LearningPathRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class LearningPathRepositoryImpl extends GenericRepositoryImpl<LearningPath> implements LearningPathRepository {

    public LearningPathRepositoryImpl() {
        super(LearningPath.class);
    }
}
