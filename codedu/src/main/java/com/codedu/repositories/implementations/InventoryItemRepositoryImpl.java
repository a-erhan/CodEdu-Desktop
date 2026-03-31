package com.codedu.repositories.implementations;

import com.codedu.models.user.InventoryItem;
import com.codedu.models.user.Item;
import com.codedu.models.user.UserInventory;
import com.codedu.repositories.interfaces.InventoryItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class InventoryItemRepositoryImpl extends GenericRepositoryImpl<InventoryItem> implements InventoryItemRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public InventoryItemRepositoryImpl() {
        super(InventoryItem.class);
    }

    @Override
    public List<InventoryItem> findByInventory(UserInventory inventory) {
        if (inventory == null || inventory.getId() == 0) {
            return List.of();
        }
        return entityManager
                .createQuery(
                        "SELECT i FROM InventoryItem i JOIN FETCH i.item it WHERE i.inventory.id = :invId AND i.isDeleted = false",
                        InventoryItem.class)
                .setParameter("invId", inventory.getId())
                .getResultList();
    }

    @Override
    public Optional<InventoryItem> findByInventoryAndItem(UserInventory inventory, Item item) {
        if (inventory == null || item == null || inventory.getId() == 0) {
            return Optional.empty();
        }
        try {
            InventoryItem row = entityManager
                    .createQuery(
                            "SELECT i FROM InventoryItem i WHERE i.inventory.id = :invId AND i.item.id = :itemId AND i.isDeleted = false",
                            InventoryItem.class)
                    .setParameter("invId", inventory.getId())
                    .setParameter("itemId", item.getId())
                    .getSingleResult();
            return Optional.of(row);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
