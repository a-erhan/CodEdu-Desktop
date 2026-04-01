package com.codedu.models.gamification;

import com.codedu.models.BaseEntity;
import com.codedu.models.learning.Reward;
import com.codedu.models.user.UserGameState;
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

    private String name;

    @Embedded
    private Reward reward;

    private String criteria;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @ManyToMany(mappedBy = "achievements", fetch = FetchType.LAZY)
    private List<UserGameState> users = new ArrayList<>();

}