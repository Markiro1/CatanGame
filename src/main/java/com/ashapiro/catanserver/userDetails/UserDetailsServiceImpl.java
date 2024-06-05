package com.ashapiro.catanserver.userDetails;

import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findUserByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        return new UserDetailsImpl(
                userEntity.getId(),
                userEntity.getToken(),
                userEntity.getLogin(),
                userEntity.getPassword(),
                userEntity.getUsername()
        );
    }
}
