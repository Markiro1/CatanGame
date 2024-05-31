package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findUserByLogin(String login);

    @Query("select u from UserEntity u left join fetch u.userToLobby utl left join fetch utl.lobby where u.token = :token")
    Optional<UserEntity> findUserByToken(String token);

    @Query("select u.login from UserEntity u where u.token = :token")
    Optional<String> findLoginByToken(@Param("token") String token);

    boolean existsByLogin(String login);

    @Query("select u.token from UserEntity u " +
            "join UserToLobby utl on utl.user.id = u.id " +
            "where utl.lobby.id = :lobbyId")
    List<String> retrieveTokensByLobbyId(Long lobbyId);

    @Query("select new com.ashapiro.catanserver.dto.user.SimpleUserDto(u.id, u.login, u.username) from UserEntity u where u.token = :token")
    Optional<SimpleUserDto> findSimpleUserByToken(String token);
}
