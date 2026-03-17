package com.codedu.repositories.implementations;

import com.codedu.models.Match;
import com.codedu.repositories.interfaces.MatchRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class MatchRepositoryImpl extends GenericRepositoryImpl<Match> implements MatchRepository {

    public MatchRepositoryImpl() {
        super(Match.class);
    }
}
