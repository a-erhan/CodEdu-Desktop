package com.codedu.services.implementations;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.*;
import com.codedu.services.interfaces.InventoryItemService;
import com.codedu.repositories.interfaces.StoreRepository;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.ItemService;
import com.codedu.services.interfaces.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StoreServiceImpl implements StoreService {

    private static final Pattern AI_PACK_QUANTITY = Pattern.compile("\\((\\d+)\\)");

    private static final String SQL_INCREMENT_HEART_VIA_USER = """
            UPDATE user_game_states AS ugs SET heart_count = LEAST(ugs.heart_count + 1, ?)
            FROM users u
            WHERE u.id = ? AND u.game_state_id = ugs.id""";

    private static final String SQL_SET_FULL_HEARTS_VIA_USER = """
            UPDATE user_game_states AS ugs SET heart_count = ?
            FROM users u
            WHERE u.id = ? AND u.game_state_id = ugs.id""";

    private final StoreRepository storeRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final UserGameStateRepository userGameStateRepository;
    private final InventoryItemService inventoryItemService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public StoreServiceImpl(
            StoreRepository storeRepository,
            ItemService itemService,
            UserRepository userRepository,
            InventoryItemService inventoryItemService,
            UserGameStateRepository userGameStateRepository
    ) {
        this.storeRepository = storeRepository;
        this.itemService = itemService;
        this.userRepository = userRepository;
        this.inventoryItemService = inventoryItemService;
        this.userGameStateRepository = userGameStateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Store> getStoreWithItemsById(int id) {
        return storeRepository.findByIdWithItems(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Store> getDefaultStoreWithItems() {
        return storeRepository.findFirstWithItemsOrderedById();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getCatalogItems() {
        List<Item> items = getCatalogItemEntities();
        return items.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public Optional<User> purchaseItem(int userId, int itemId) {
        if (userId <= 0 || itemId <= 0) return Optional.empty();

        User user = userRepository.findByIdWithInventoryAndGameState(userId).orElse(null);
        if (user == null || user.getGameState() == null) {
            return Optional.empty();
        }

        Item item = itemService.getItemById(itemId).orElse(null);
        if (item == null || item.isDeleted()) return Optional.empty();

        int price = Math.max(0, item.getPrice());
        com.codedu.models.user.UserGameState gs = user.getGameState();
        if (!gs.hasEnoughTokens(price)) return Optional.empty();

        String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";
        boolean heartByName = name.contains("heart");

        if (heartByName && !name.contains("full")
                && gs.getHeartCount() >= UserGameState.MAX_HEARTS) {
            return Optional.empty();
        }

        inventoryItemService.addOrIncrementItemQuantity(user, item, purchaseInventoryDelta(item));

        if (item.getType() == ItemType.BOOSTER && name.contains("xp")) {
            if (name.contains("mega")) {
                gs.addXpAndResolveLevelUps(gs.withDoubleXpApplied(500));
            } else if (name.contains("double")) {
                gs.extendDoubleXpMinutes(30);
            }
        }

        gs.setTokenBalance(gs.getTokenBalance() - price);

        userGameStateRepository.update(gs);
        userRepository.update(user);

        entityManager.flush();

        if (heartByName) {
            int rows;
            if (name.contains("full")) {
                rows = entityManager.createNativeQuery(SQL_SET_FULL_HEARTS_VIA_USER)
                        .setParameter(1, UserGameState.MAX_HEARTS)
                        .setParameter(2, userId)
                        .executeUpdate();
                if (rows == 0) {
                    rows = entityManager.createQuery(
                                    "UPDATE UserGameState g SET g.heartCount = :max WHERE g.user.id = :uid")
                            .setParameter("max", UserGameState.MAX_HEARTS)
                            .setParameter("uid", userId)
                            .executeUpdate();
                }
            } else {
                rows = entityManager.createNativeQuery(SQL_INCREMENT_HEART_VIA_USER)
                        .setParameter(1, UserGameState.MAX_HEARTS)
                        .setParameter(2, userId)
                        .executeUpdate();
                if (rows == 0) {
                    rows = entityManager.createQuery(
                                    "UPDATE UserGameState g SET g.heartCount = g.heartCount + 1 "
                                            + "WHERE g.user.id = :uid AND g.heartCount < :max")
                            .setParameter("uid", userId)
                            .setParameter("max", UserGameState.MAX_HEARTS)
                            .executeUpdate();
                }
            }
            entityManager.flush();
        }

        entityManager.clear();
        return userRepository.findByIdWithInventoryAndGameState(userId);
    }

    private static int purchaseInventoryDelta(Item item) {
        if (item.getType() != ItemType.AI_USAGE || item.getName() == null) {
            return 1;
        }
        Matcher m = AI_PACK_QUANTITY.matcher(item.getName());
        if (m.find()) {
            try {
                return Math.max(1, Integer.parseInt(m.group(1)));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        return 1;
    }

    @Override
    @Transactional
    public void saveStore(Store store) {
        storeRepository.save(store);
    }

    @Override
    @Transactional
    public void updateStore(Store store) {
        storeRepository.update(store);
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

    @Override
    @Transactional(readOnly = true)
    public List<Item> getCatalogItemEntities() {
        Optional<Store> storeOpt = storeRepository.findFirstWithItemsOrderedById();
        if (storeOpt.isPresent()) {
            List<Item> fromStore = storeOpt.get().getAvailableItems().stream()
                    .filter(i -> !i.isDeleted())
                    .toList();
            if (!fromStore.isEmpty()) {
                return new ArrayList<>(fromStore);
            }
        }
        return new ArrayList<>(itemService.getAllItemEntities());
    }
}