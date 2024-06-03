package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.entity.UserToLobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserToLobbyRepository extends JpaRepository<UserToLobby, Long> {

    void deleteByUserId(Long userId);

}
