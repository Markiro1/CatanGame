package com.ashapiro.catanserver.userDetails;

import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.info("loadUserByUsername(): Loading user by username...");
        User user = userRepository.findUserByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        return new UserDetailsImpl(
                user.getId(),
                user.getToken(),
                user.getLogin(),
                user.getPassword(),
                user.getUsername()
        );
    }
}
