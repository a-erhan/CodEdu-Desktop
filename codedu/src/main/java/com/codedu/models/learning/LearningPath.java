package com.codedu.models.learning;

import com.codedu.models.BaseEntity;
import com.codedu.models.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id")
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    public boolean isCompleted(User user) {
        if (chapters == null || chapters.isEmpty()) {
            return false;
        }

        for (Chapter chapter : chapters) {
            if (chapter == null || !chapter.isCompleted()) {
                return false;
            }
        }

        return true;
    }
}