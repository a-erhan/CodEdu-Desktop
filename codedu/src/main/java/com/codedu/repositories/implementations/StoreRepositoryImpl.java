package com.codedu.repositories.implementations;

import com.codedu.models.user.Store;
import com.codedu.repositories.interfaces.StoreRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class StoreRepositoryImpl extends GenericRepositoryImpl<Store> implements StoreRepository {

    public StoreRepositoryImpl() {
        super(Store.class);
    }
}
