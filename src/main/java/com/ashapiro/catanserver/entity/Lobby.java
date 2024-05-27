package com.ashapiro.catanserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "lobbies")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL)
    private Set<UserToLobby> usersToLobby = new HashSet<>();

    public void removeByUser(User user) {
        usersToLobby.remove(user.getUserToLobby());
        user.setUserToLobby(null);
    }

    public List<String> getAllTokenUsersInLobby() {
        return usersToLobby.stream()
                .map(UserToLobby::getUser)
                .map(User::getToken)
                .collect(Collectors.toList());
    }
}
