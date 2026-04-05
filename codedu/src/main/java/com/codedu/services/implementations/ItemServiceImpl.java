package com.codedu.services.implementations;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.ItemRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemDTO> getItemDTOById(int id) {
        return itemRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Item> getItemById(int id) {
        return itemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getAllItems() {
        return itemRepository.getAll().stream()
                .filter(item -> !item.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getAllItemEntities() {
        return itemRepository.getAll().stream()
                .filter(item -> !item.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByType(ItemType type) {
        return itemRepository.findByType(type).stream()
                .filter(item -> !item.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemDTO> getItemByName(String name) {
        return itemRepository.findByName(name)
                .filter(item -> !item.isDeleted())
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Override
    @Transactional
    public void updateItem(Item item) {
        itemRepository.update(item);
    }

    /**
     * Applies the logic/effect of an item to a user based on its ItemType.
     * This makes the service "aware" of your gamification rules.
     */
    @Override
    @Transactional
    public void applyItemEffect(int userId, int itemId) {
        User user = userRepository.findByIdWithInventoryAndGameState(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        UserGameState gs = user.getGameState();
        if (gs == null) return;

        String name = item.getName() != null ? item.getName().toLowerCase() : "";

        switch (item.getType()) {
            case BOOSTER -> {
                if (name.contains("heart")) {
                    if (name.contains("full")) {
                        gs.setHeartCount(UserGameState.MAX_HEARTS);
                    } else {
                        gs.addHeart();
                    }
                } else if (name.contains("xp")) {
                    if (name.contains("mega")) {
                        gs.addXpAndResolveLevelUps(gs.withDoubleXpApplied(500));
                    } else if (name.contains("double")) {
                        gs.extendDoubleXpMinutes(30);
                    }
                }
            }
            case HEART -> {
                if (name.contains("full")) {
                    gs.setHeartCount(UserGameState.MAX_HEARTS);
                } else {
                    gs.addHeart();
                }
            }
            case AI_USAGE -> {
            }
            case AVATAR -> {
                // Avatars are handled by the 'Equip' logic in the Inventory
            }
        }

        userRepository.update(user);
    }

    /**
     * Helper to convert Entity to DTO for the UI.
     */
    private ItemDTO toDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .iconURL(item.getIconURL())
                .price(item.getPrice())
                .type(item.getType())
                .owned(item.isOwned())
                .build();
    }
}