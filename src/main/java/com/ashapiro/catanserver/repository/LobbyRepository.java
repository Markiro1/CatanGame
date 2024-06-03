package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDTO;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LobbyRepository extends JpaRepository<LobbyEntity, Long> {

    @Query("select new com.ashapiro.catanserver.dto.lobby.AllLobbyDTO(" +
            "l.id, " +
            "l.name," +
            "count (u)" +
            ") from LobbyEntity l " +
            "join UserToLobby utl on utl.lobby.id = l.id " +
            "join UserEntity u on utl.user.id = u.id " +
            "group by l.id")
    List<AllLobbyDTO> findAllLobbies();

    @Query("select distinct l " +
            "from LobbyEntity l " +
            "left join fetch UserToLobby utl on utl.lobby = l " +
            "join UserEntity u on utl.user = u " +
            "where u = :user")
    Optional<LobbyEntity> findLobbyByUser(UserEntity user);

    @Query("select l from LobbyEntity l left join fetch l.usersToLobby where l.id = :id")
    Optional<LobbyEntity> findLobbyByIdFetchUserToLobby(Long id);
}
