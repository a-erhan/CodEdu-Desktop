package com.codedu.dtos.user;

import com.codedu.models.user.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private int id;
    private String name;
    private String description;
    private String iconURL;
    private int price;
    private ItemType type;
    private boolean owned;
}
