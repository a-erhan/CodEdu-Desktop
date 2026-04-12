package com.codedu.models.matchmaking;

import com.codedu.models.learning.CodeImplementationQuestion;
import com.codedu.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameRoom {

    private String roomId;
    private User player1;
    private User player2;
    private CodeImplementationQuestion question;
}
