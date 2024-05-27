package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDto;
import com.ashapiro.catanserver.dto.lobby.LobbyDetailsDto;
import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LobbyRepository extends JpaRepository<Lobby, Long> {

    @Query("select new com.ashapiro.catanserver.dto.lobby.AllLobbyDto(" +
            "l.id, " +
            "l.name," +
            "count (u)" +
            ") from Lobby l " +
            "join UserToLobby utl on utl.lobby.id = l.id " +
            "join User u on utl.user.id = u.id " +
            "group by l.id")
    List<AllLobbyDto> findAllLobbies();

    @Query("select distinct l " +
            "from Lobby l " +
            "left join fetch UserToLobby utl on utl.lobby = l " +
            "join User u on utl.user = u " +
            "where u = :user")
    Optional<Lobby> findLobbyByUser(User user);

    @Query("select l from Lobby l left join fetch l.usersToLobby where l.id = :id")
    Optional<Lobby> findLobbyByIdFetchUserToLobby(Long id);
}
