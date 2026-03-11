package com.codedu.repositories.interfaces;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T> {
    void save(T entity);
    void update(T entity);
    Optional<T> findById(int id);
    void hardDelete(int id);
    void softDelete(int id);
    List<T> getAll();
}
