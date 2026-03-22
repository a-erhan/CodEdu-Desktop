package com.codedu.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserProfileDTO {
    private Long userId;
    private String username;
    private int level;
    private int xp;
    private int xpToNextLevel;
    private int tokenBalance;
    private int totalItemsOwned;

    private String relationStatus;

    private List<BadgeDTO> badges;

    @Data
    @Builder
    public static class BadgeDTO {
        private String icon;
        private String name;
        private String description;
    }
}
