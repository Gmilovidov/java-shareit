package ru.practicum.shareit.jpaRepositoryTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRequestRepositoryTest {

    @Autowired
    ItemRequestRepository itemRequestRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    Item item;
    Item item2;
    User owner;
    User requestor;
    ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        owner = User.builder()
                .name("User")
                .email("user@email.com")
                .build();

        item = Item.builder()
                .name("Item")
                .description("Item Description")
                .available(true)
                .owner(owner)
                .build();

        item2 = Item.builder()
                .name("Item2")
                .description("Item2 Descr")
                .available(true)
                .owner(owner)
                .build();

        requestor = User.builder()
                .name("requestor")
                .email("requestor@mail.ru")
                .build();

        itemRequest = ItemRequest.builder()
                .description("description")
                .created(LocalDateTime.now())
                .items(List.of(item, item2))
                .requestor(requestor)
                .build();
    }

    @Test
    void shouldReturnAllRequestForRequestor() {
        Pageable pageable = PageRequest.of(0, 10);
        userRepository.save(owner);
        userRepository.save(requestor);
        itemRepository.save(item);
        itemRepository.save(item2);
        itemRequestRepository.save(itemRequest);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNot(1L, pageable);

        assertEquals(1, requests.size());
        assertEquals(itemRequest.getItems(), List.of(item, item2));
        assertEquals(itemRequest.getDescription(), requests.get(0).getDescription());

    }
}
