package com.codedu.repositories.implementations;

import com.codedu.models.matchmaking.LeaderBoard;
import com.codedu.repositories.interfaces.LeaderBoardRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class LeaderBoardRepositoryImpl extends GenericRepositoryImpl<LeaderBoard> implements LeaderBoardRepository {

    public LeaderBoardRepositoryImpl() {
        super(LeaderBoard.class);
    }
}
