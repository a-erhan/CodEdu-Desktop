package com.codedu.repositories.implementations;

import com.codedu.models.MatchTask;
import com.codedu.repositories.interfaces.MatchTaskRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class MatchTaskRepositoryImpl extends GenericRepositoryImpl<MatchTask> implements MatchTaskRepository {

    public MatchTaskRepositoryImpl() {
        super(MatchTask.class);
    }
}
