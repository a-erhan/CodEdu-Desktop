package com.codedu.seeders;

import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.models.user.Store;
import com.codedu.models.user.UserGameState;
import com.codedu.repositories.interfaces.ItemRepository;
import com.codedu.repositories.interfaces.StoreRepository;
import com.codedu.repositories.interfaces.UserGameStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(5)
public class StoreItemSeeder implements CommandLineRunner {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserGameStateRepository userGameStateRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedAiItems();
        seedAvatarItems();
        seedBoosterItems();
        ensureStoreHasAllItems();
        grantStarterTokensToExistingUsers();
    }

    private void seedBoosterItems() {
        List<Item> existing = itemRepository.findByType(ItemType.BOOSTER);
        if (!existing.isEmpty()) {
            for (Item i : existing) {
                if ("Full Heart Pack".equals(i.getName()) || "Full Heart Recharge".equals(i.getName())) {
                    i.setName("Full Heart Recharge");
                    i.setDescription("Sets hearts to the maximum (15). Use from Inventory after purchase.");
                    itemRepository.update(i);
                }
            }
            return;
        }

        System.out.println(">>> [StoreSeeder] Seeding booster items...");
        // Heart Refills
        itemRepository.save(new Item("Heart Refill", "Instantly restores 1 heart.", null, 30, ItemType.BOOSTER));
        itemRepository.save(new Item("Full Heart Recharge", "Sets hearts to the maximum (15). Use from Inventory after purchase.", null, 100, ItemType.BOOSTER));

        // XP Boosters
        itemRepository.save(new Item("Double XP (30m)", "Earn 2x XP for the next 30 minutes.", null, 150, ItemType.BOOSTER));
        itemRepository.save(new Item("XP Mega-Boost", "Instantly gain 500 XP.", null, 400, ItemType.BOOSTER));
    }

    private void seedAiItems() {
        List<Item> existing = itemRepository.findByType(ItemType.AI_USAGE);
        if (!existing.isEmpty()) return;

        System.out.println(">>> [StoreSeeder] Seeding AI request token items...");
        itemRepository.save(new Item("AI Request Pack (5)", "Grants 5 AI tutor requests.", null, 50, ItemType.AI_USAGE));
        itemRepository.save(new Item("AI Request Pack (15)", "Grants 15 AI tutor requests.", null, 120, ItemType.AI_USAGE));
    }

    private void seedAvatarItems() {
        List<Item> existing = itemRepository.findByType(ItemType.AVATAR);
        if (!existing.isEmpty()) return;

        System.out.println(">>> [StoreSeeder] Seeding avatar items...");
        itemRepository.save(new Item("Basic Avatar", "The classic look.", null, 0, ItemType.AVATAR));
        itemRepository.save(new Item("Ninja Avatar", "Silent and swift.", null, 200, ItemType.AVATAR));
        itemRepository.save(new Item("Wizard Avatar", "Wise and powerful.", null, 500, ItemType.AVATAR));
        // Added a high-end one for "whales"
        itemRepository.save(new Item("Cyberpunk Legend", "Exclusive neon-lit style.", null, 1500, ItemType.AVATAR));
    }

    private void grantStarterTokensToExistingUsers() {
        List<UserGameState> states = userGameStateRepository.getAll();
        int granted = 0;
        for (UserGameState state : states) {
            // Check for 0 balance specifically to avoid spamming existing rich users
            if (state.getTokenBalance() == 0) {
                state.setTokenBalance(100);
                userGameStateRepository.update(state);
                granted++;
            }
        }
        if (granted > 0) {
            System.out.println(">>> [StoreSeeder] Granted 100 starter tokens to " + granted + " users.");
        }
    }

    private void ensureStoreHasAllItems() {
        Store store = storeRepository.findFirstWithItemsOrderedById().orElse(null);
        if (store == null) {
            store = new Store();
            store.setAvailableItems(new ArrayList<>());
            storeRepository.save(store);
        }

        List<Item> allItems = itemRepository.getAll();
        for (Item item : allItems) {
            boolean alreadyInStore = store.getAvailableItems().stream()
                    .anyMatch(i -> i.getId() == item.getId());
            if (!alreadyInStore) {
                store.getAvailableItems().add(item);
            }
        }
        storeRepository.update(store);
        System.out.println(">>> [StoreSeeder] Store synced with " + store.getAvailableItems().size() + " items.");
    }
}