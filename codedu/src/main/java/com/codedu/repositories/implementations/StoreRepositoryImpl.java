package com.codedu.repositories.implementations;

import com.codedu.models.user.Store;
import com.codedu.repositories.interfaces.StoreRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class StoreRepositoryImpl extends GenericRepositoryImpl<Store> implements StoreRepository {

    public StoreRepositoryImpl() {
        super(Store.class);
    }

    @Override
    public Optional<Store> findByIdWithItems(int id) {
        return getEntityManager()
                .createQuery(
                        "SELECT DISTINCT s FROM Store s " +
                                "LEFT JOIN FETCH s.availableItems ai " +
                                "WHERE s.id = :id AND s.isDeleted = false",
                        Store.class
                )
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Store> findFirstWithItemsOrderedById() {
        return getEntityManager()
                .createQuery(
                        "SELECT DISTINCT s FROM Store s " +
                                "LEFT JOIN FETCH s.availableItems ai " +
                                "WHERE s.isDeleted = false " +
                                "ORDER BY s.id ASC",
                        Store.class
                )
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
