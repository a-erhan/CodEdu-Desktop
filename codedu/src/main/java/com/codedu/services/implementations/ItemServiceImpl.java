package com.codedu.services.implementations;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.ItemRepository;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Pattern AI_PACK_QUANTITY = Pattern.compile("\\((\\d+)\\)");

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final UserGameStateRepository userGameStateRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ItemServiceImpl(
            ItemRepository itemRepository,
            UserRepository userRepository,
            UserGameStateRepository userGameStateRepository
    ) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.userGameStateRepository = userGameStateRepository;
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
    @Transactional
    public void applyItemEffect(int userId, int itemId) {

        User user = userRepository.findByIdWithInventoryAndGameState(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));

        UserGameState gs = user.getGameState();
        if (gs == null) return;

        String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";

        if (item.getType() == ItemType.BOOSTER && name.contains("xp")) {
            if (name.contains("mega")) {
                gs.addXpAndResolveLevelUps(gs.withDoubleXpApplied(500));
            } else if (name.contains("double")) {
                gs.extendDoubleXpMinutes(30);
            }
        }

        if (item.getType() == ItemType.BOOSTER && name.contains("heart")) {
            if (name.contains("full")) {
                gs.setHeartCount(UserGameState.MAX_HEARTS);
            } else {
                if (gs.getHeartCount() < UserGameState.MAX_HEARTS) {
                    gs.setHeartCount(gs.getHeartCount() + 1);
                }
            }
        }

        userGameStateRepository.update(gs);

        entityManager.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getAllItems() {
        return itemRepository.getAll().stream().filter(i -> !i.isDeleted()).map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getAllItemEntities() {
        return itemRepository.getAll().stream().filter(i -> !i.isDeleted()).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByType(ItemType type) {
        return itemRepository.findByType(type).stream().filter(i -> !i.isDeleted()).map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemDTO> getItemByName(String name) {
        return itemRepository.findByName(name).filter(i -> !i.isDeleted()).map(this::toDTO);
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

    @Override
    public int getGrantedQuantity(Item item) {
        if (item.getType() != ItemType.AI_USAGE || item.getName() == null) {
            return 1;
        }
        Matcher m = AI_PACK_QUANTITY.matcher(item.getName());
        if (m.find()) {
            try { return Math.max(1, Integer.parseInt(m.group(1))); }
            catch (NumberFormatException ignored) {}
        }
        return 1;
    }

    @Override
    public boolean isEffectRedundant(User user, Item item) {
        UserGameState gs = user.getGameState();
        if (gs == null) return false;

        String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";

        if (name.contains("heart") && !name.contains("full") && gs.getHeartCount() >= UserGameState.MAX_HEARTS) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void applyItemEffect(User user, Item item) {
        UserGameState gs = user.getGameState();
        if (gs == null) return;

        String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";

        if (item.getType() == ItemType.BOOSTER && name.contains("xp")) {
            if (name.contains("mega")) {
                gs.addXpAndResolveLevelUps(gs.withDoubleXpApplied(500));
            } else if (name.contains("double")) {
                gs.extendDoubleXpMinutes(30);
            }
        }

        if (name.contains("heart")) {
            if (name.contains("full")) {
                gs.setHeartCount(UserGameState.MAX_HEARTS);
            } else {
                if (gs.getHeartCount() < UserGameState.MAX_HEARTS) {
                    gs.setHeartCount(gs.getHeartCount() + 1);
                }
            }
        }

        userGameStateRepository.update(gs);

        entityManager.flush();
    }

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