package com.codedu.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
public class Achievement extends BaseEntity {

    private String badgeName;
    private String description;
    private int tokenReward;

    @ManyToMany(mappedBy = "achievements", fetch = FetchType.LAZY)
    private List<UserGameState> usersAchieved = new ArrayList<>();

    private String iconPath;
}