package com.codedu.services.implementations;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.Store;
import com.codedu.models.user.User;
import com.codedu.models.user.UserGameState;
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
import java.util.Optional;

@Service
public class StoreServiceImpl implements StoreService {

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

        // 1. Fetch User and Item
        User user = userRepository.findByIdWithInventoryAndGameState(userId).orElse(null);
        if (user == null || user.getGameState() == null) return Optional.empty();

        Item item = itemService.getItemById(itemId).orElse(null);
        if (item == null || item.isDeleted()) return Optional.empty();

        // 2. Validate Price & Redundancy
        int price = Math.max(0, item.getPrice());
        UserGameState gs = user.getGameState();

        if (!gs.hasEnoughTokens(price)) return Optional.empty();
        if (itemService.isEffectRedundant(user, item)) return Optional.empty();

        // 3. Grant Item to Inventory
        int quantity = itemService.getGrantedQuantity(item);
        inventoryItemService.addOrIncrementItemQuantity(user, item, quantity);

        // 4. Deduct Tokens
        gs.setTokenBalance(gs.getTokenBalance() - price);
        userGameStateRepository.update(gs);
        userRepository.update(user);

        // Save the token deduction before any native SQL effects run
        entityManager.flush();

        // 5. Delegate the heavy lifting (Game Logic) to Item Service
        itemService.applyItemEffect(user, item);

        // 6. Clear session and fetch fresh state for the UI
        entityManager.clear();
        return userRepository.findByIdWithInventoryAndGameState(userId);
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