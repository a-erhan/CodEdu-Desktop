package com.codedu.services.implementations;

import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.repositories.interfaces.ItemRepository;
import com.codedu.services.interfaces.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Item> getItemById(int id) {
        return itemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getAllItems() {
        return itemRepository.getAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getItemsByType(ItemType type) {
        return itemRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Item> getItemByName(String name) {
        return itemRepository.findByName(name);
    }

    @Override
    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Override
    @Transactional
    public void updateItem(Item item) {
        itemRepository.update(item);
    }
}
