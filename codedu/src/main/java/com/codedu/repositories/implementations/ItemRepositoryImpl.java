package com.codedu.repositories.implementations;

import com.codedu.models.Item;
import com.codedu.models.ItemType;
import com.codedu.repositories.interfaces.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ItemRepositoryImpl extends GenericRepositoryImpl<Item> implements ItemRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ItemRepositoryImpl() {
        super(Item.class);
    }

    @Override
    public List<Item> findByType(ItemType type) {
        return entityManager.createQuery(
                "SELECT i FROM Item i WHERE i.type = :type AND i.isDeleted = false", 
                Item.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public Optional<Item> findByName(String name) {
        try {
            Item item = entityManager.createQuery(
                    "SELECT i FROM Item i WHERE i.name = :name AND i.isDeleted = false", 
                    Item.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(item);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
