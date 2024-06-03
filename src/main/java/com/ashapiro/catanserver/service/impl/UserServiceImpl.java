package com.ashapiro.catanserver.service.impl;

import com.ashapiro.catanserver.dto.auth.RegisterDTO;
import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.repository.UserRepository;
import com.ashapiro.catanserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public SimpleUserDTO save(RegisterDTO registerDto) {
        validateLogin(registerDto.getLogin());
        UserEntity user = createUserFromRequest(registerDto);
        userRepository.save(user);
        return new SimpleUserDTO(user.getId(), user.getUsername());
    }

    @Transactional
    @Override
    public void updateUserTokenByLogin(String login, String token) {
        UserEntity user = userRepository.findUserByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        user.setToken(token);
    }

    @Override
    public Optional<UserEntity> findUserEntityByToken(String token) {
        return userRepository.findUserByToken(token);
    }

    private UserEntity createUserFromRequest(RegisterDTO registerDto) {
        UserEntity user = convertToUserFromRequest(registerDto);
        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
        user.setPassword(encodedPassword);
        return user;
    }

    private UserEntity convertToUserFromRequest(RegisterDTO registerDto) {
        return modelMapper.map(registerDto, UserEntity.class);
    }

    private void validateLogin(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new NoSuchElementException("User does not exist");
        }
    }
}
