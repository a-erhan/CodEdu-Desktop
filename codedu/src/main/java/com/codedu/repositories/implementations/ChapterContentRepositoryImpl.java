package com.codedu.repositories.implementations;

import com.codedu.models.ChapterContent;
import com.codedu.repositories.interfaces.ChapterContentRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class ChapterContentRepositoryImpl extends GenericRepositoryImpl<ChapterContent> implements ChapterContentRepository {

    public ChapterContentRepositoryImpl() {
        super(ChapterContent.class);
    }
}
