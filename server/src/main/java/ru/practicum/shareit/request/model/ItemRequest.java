package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "requestor_id", referencedColumnName = "id")
    private User requestor;

    @Transient
    private List<Item> items;
}
