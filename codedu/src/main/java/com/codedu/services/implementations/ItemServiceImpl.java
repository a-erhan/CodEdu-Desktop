package com.codedu.services.implementations;

import com.codedu.dtos.user.ItemDTO;
import com.codedu.models.user.Item;
import com.codedu.models.user.ItemType;
import com.codedu.repositories.interfaces.ItemRepository;
import com.codedu.services.interfaces.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemDTO> getItemDTOById(int id) {
        return itemRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Item> getItemById(int id) {
        return itemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getAllItems() {
        return itemRepository.getAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByType(ItemType type) {
        return itemRepository.findByType(type).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemDTO> getItemByName(String name) {
        return itemRepository.findByName(name).map(this::toDTO);
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
    public List<Item> getAllItemEntities() {
        return itemRepository.getAll();
    }
}
