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
public class LobbyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL)
    private Set<UserToLobby> usersToLobby = new HashSet<>();

    public void removeByUser(UserEntity userEntity) {
        usersToLobby.remove(userEntity.getUserToLobby());
        userEntity.setUserToLobby(null);
    }

    public List<String> getAllTokenUsersInLobby() {
        return usersToLobby.stream()
                .map(UserToLobby::getUser)
                .map(UserEntity::getToken)
                .collect(Collectors.toList());
    }
}
