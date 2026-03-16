package com.codedu.repositories.implementations;

import com.codedu.models.UserInventory;
import com.codedu.repositories.interfaces.UserInventoryRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserInventoryRepositoryImpl extends GenericRepositoryImpl<UserInventory> implements UserInventoryRepository {

    public UserInventoryRepositoryImpl() {
        super(UserInventory.class);
    }
}
