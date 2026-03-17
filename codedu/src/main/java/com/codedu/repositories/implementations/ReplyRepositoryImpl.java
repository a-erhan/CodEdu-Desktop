package com.codedu.repositories.implementations;

import com.codedu.models.Reply;
import com.codedu.repositories.interfaces.ReplyRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class ReplyRepositoryImpl extends GenericRepositoryImpl<Reply> implements ReplyRepository {

    public ReplyRepositoryImpl() {
        super(Reply.class);
    }
}
