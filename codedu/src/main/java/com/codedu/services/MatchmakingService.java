package com.codedu.services;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
//TODO this class will be rewritten later March 15
public class MatchmakingService {

    private final ConcurrentLinkedQueue<Long> playerQueue = new ConcurrentLinkedQueue<>();

    /**
     * Add player to the matchmaking queue
     */
    public void joinQueue(Long playerId) {
        if (!playerQueue.contains(playerId)) {
            playerQueue.add(playerId);
            System.out.println("Oyuncu " + playerId + " sıraya girdi. Kuyruktaki kişi sayısı: " + playerQueue.size());

            // Check for match after every
            checkForMatch();
        }
    }

    /**
     * Oyuncu sıradan çıkmak isterse (İptal butonu)
     */
    public void leaveQueue(Long playerId) {
        playerQueue.remove(playerId);
        System.out.println("Oyuncu " + playerId + " sıradan ayrıldı.");
    }

    /**
     * Match two top people for now
     * TODO Matching logic may be changed
     */
    private void checkForMatch() {
        if (playerQueue.size() >= 2) {
            Long player1Id = playerQueue.poll();
            Long player2Id = playerQueue.poll();

            System.out.println("Found match between Player " + player1Id + " and Player " + player2Id);

            // TODO: Match objects will be initiated here
            startMatch(player1Id, player2Id);
        }
    }

    /**
     * Eşleşen oyuncular için maçı başlatacak geçici metod.
     */
    private void startMatch(Long p1, Long p2) {
        System.out.println("Initaiating the match room for : " + p1 + " ve " + p2 );
    }
}
