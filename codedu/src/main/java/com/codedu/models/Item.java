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
}