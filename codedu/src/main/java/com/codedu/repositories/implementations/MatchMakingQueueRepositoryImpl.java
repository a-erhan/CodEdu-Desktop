package com.codedu.repositories.implementations;

import com.codedu.models.matchmaking.MatchMakingQueue;
import com.codedu.repositories.interfaces.MatchMakingQueueRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class MatchMakingQueueRepositoryImpl extends GenericRepositoryImpl<MatchMakingQueue> implements MatchMakingQueueRepository {

    public MatchMakingQueueRepositoryImpl() {
        super(MatchMakingQueue.class);
    }
}
