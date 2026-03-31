package com.codedu.models.matchmaking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the outcome of a 1v1 match.
 * Sent by the winning client to /app/match.win, then
 * broadcast by the server to both players via STOMP.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResult {

    private String roomId;
    private int winnerId;
    private int loserId;
    private String winnerUsername;
}
