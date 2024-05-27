package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.entity.UserToLobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserToLobbyRepository extends JpaRepository<UserToLobby, Long> {

    void deleteByUserId(Long userId);

    @Query("select utl.lobby.id from UserToLobby utl where utl.user.id = :userId")
    Long getLobbyIdByUserId(Long userId);
}
