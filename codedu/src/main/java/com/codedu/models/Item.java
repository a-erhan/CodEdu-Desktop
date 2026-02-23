package com.codedu.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {

    private String name;
    private String description;
    private String emoji;
    private int price;

    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Transient
    private boolean owned;

    public Item(String name, String description, String emoji, int price, ItemType type) {
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.price = price;
        this.type = type;
    }
}