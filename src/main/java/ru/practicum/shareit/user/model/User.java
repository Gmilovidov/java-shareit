package ru.practicum.shareit.user.model;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String name;

    public static User buildUser(User user) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
