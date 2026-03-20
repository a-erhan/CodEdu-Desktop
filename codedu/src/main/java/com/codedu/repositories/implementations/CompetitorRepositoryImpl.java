package com.codedu.repositories.implementations;

import com.codedu.models.matchmaking.Competitor;
import com.codedu.repositories.interfaces.CompetitorRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class CompetitorRepositoryImpl extends GenericRepositoryImpl<Competitor> implements CompetitorRepository {

    public CompetitorRepositoryImpl() {
        super(Competitor.class);
    }
}
