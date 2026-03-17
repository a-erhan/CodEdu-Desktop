package com.codedu.repositories.implementations;

import com.codedu.models.ForumPost;
import com.codedu.repositories.interfaces.ForumPostRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class ForumPostRepositoryImpl extends GenericRepositoryImpl<ForumPost> implements ForumPostRepository {

    public ForumPostRepositoryImpl() {
        super(ForumPost.class);
    }
}
