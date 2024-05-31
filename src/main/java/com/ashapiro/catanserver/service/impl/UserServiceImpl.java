package com.ashapiro.catanserver.service.impl;

import com.ashapiro.catanserver.dto.auth.RegisterDto;
import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.repository.UserRepository;
import com.ashapiro.catanserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public SimpleUserDto save(RegisterDto registerDto) {
        validateLogin(registerDto.getLogin());
        UserEntity user = createUserFromRequest(registerDto);
        userRepository.save(user);
        return new SimpleUserDto(user.getId(), user.getUsername());
    }

    @Transactional
    @Override
    public void updateUserTokenByLogin(String login, String token) {
        UserEntity user = userRepository.findUserByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        user.setToken(token);
    }

    @Override
    public Optional<UserEntity> findUserByLogin(String login) {
        return userRepository.findUserByLogin(login);
    }

    @Override
    public Optional<UserEntity> findUserByToken(String token) {
        return userRepository.findUserByToken(token);
    }

    @Override
    public List<String> retrieveTokensByLobbyId(Long lobbyId) {
        return userRepository.retrieveTokensByLobbyId(lobbyId);
    }

    @Override
    public Optional<SimpleUserDto> findSimpleUserByToken(String token) {
        return userRepository.findSimpleUserByToken(token);
    }

    private UserEntity createUserFromRequest(RegisterDto registerDto) {
        UserEntity user = convertToUserFromRequest(registerDto);
        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
        user.setPassword(encodedPassword);
        return user;
    }

    private UserEntity convertToUserFromRequest(RegisterDto registerDto) {
        return modelMapper.map(registerDto, UserEntity.class);
    }

    private void validateLogin(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new NoSuchElementException("User does not exist");
        }
    }
}
