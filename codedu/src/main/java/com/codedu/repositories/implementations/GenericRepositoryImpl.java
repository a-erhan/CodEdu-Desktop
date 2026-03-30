package com.codedu.repositories.implementations;

import com.codedu.models.BaseEntity;
import com.codedu.models.user.User;
import com.codedu.repositories.interfaces.GenericRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Transactional
public abstract class GenericRepositoryImpl<T extends BaseEntity> implements GenericRepository<T> {

    @Getter
    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityClass;

    public GenericRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        entityManager.persist(entity);
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }

    @Override
    public Optional<T> findById(int id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Override
    public void hardDelete(int id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public void softDelete(int id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entity.setDeleted(true);
            entityManager.merge(entity);
        }
    }

    @Override
    public List<T> getAll() {
        return entityManager.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.isDeleted = false", entityClass)
                .getResultList();
    }

}