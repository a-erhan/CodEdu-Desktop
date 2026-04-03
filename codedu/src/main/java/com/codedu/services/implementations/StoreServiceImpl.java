package com.codedu.services.implementations;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.Store;
import com.codedu.models.user.User;
import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.UserInventory;
import com.codedu.repositories.interfaces.InventoryItemRepository;
import com.codedu.repositories.interfaces.StoreRepository;
import com.codedu.repositories.interfaces.UserRepository;
import com.codedu.services.interfaces.ItemService;
import com.codedu.services.interfaces.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public StoreServiceImpl(
            StoreRepository storeRepository,
            ItemService itemService,
            UserRepository userRepository,
            InventoryItemRepository inventoryItemRepository
    ) {
        this.storeRepository = storeRepository;
        this.itemService = itemService;
        this.userRepository = userRepository;
        this.inventoryItemRepository = inventoryItemRepository;
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
        Optional<Store> storeOpt = storeRepository.findFirstWithItemsOrderedById();
        if (storeOpt.isPresent()) {
            List<ItemDTO> fromStore = storeOpt.get().getAvailableItems().stream()
                    .filter(i -> !i.isDeleted())
                    .map(this::toDTO)
                    .toList();
            if (!fromStore.isEmpty()) {
                return new ArrayList<>(fromStore);
            }
        }
        return new ArrayList<>(itemService.getAllItems());
    }

    @Override
    @Transactional
    public Optional<User> purchaseItem(int userId, int itemId) {
        if (userId <= 0 || itemId <= 0) {
            return Optional.empty();
        }

        User user = userRepository.findByIdWithInventoryAndGameState(userId).orElse(null);
        if (user == null || user.getGameState() == null) {
            return Optional.empty();
        }

        Item item = itemService.getItemById(itemId).orElse(null);
        if (item == null || item.isDeleted()) {
            return Optional.empty();
        }

        int price = Math.max(0, item.getPrice());
        if (!user.getGameState().hasEnoughTokens(price)) {
            return Optional.empty();
        }

        UserInventory inv = user.getInventory();
        if (inv == null) {
            inv = new UserInventory();
            user.setInventory(inv);
        }

        InventoryItem row = inventoryItemRepository.findByInventoryAndItem(inv, item).orElse(null);
        if (row == null) {
            row = InventoryItem.builder()
                    .inventory(inv)
                    .item(item)
                    .quantity(1)
                    .isEquipped(false)
                    .build();
            inv.addItem(row);
        } else {
            row.setQuantity(row.getQuantity() + 1);
        }

        user.getGameState().setTokenBalance(user.getGameState().getTokenBalance() - price);
        userRepository.update(user);

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
