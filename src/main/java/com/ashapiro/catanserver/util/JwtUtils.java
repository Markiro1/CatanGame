package com.ashapiro.catanserver.util;

import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.userDetails.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final UserService userService;

    public String generateToken(UserDetailsImpl userDetails) {
        String secretKey = generateSecretKey(userDetails);

        LocalDateTime expiredTime = LocalDateTime.now().plusDays(1);
        Date expiredDate = Date.from(expiredTime.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .claim("userId", userDetails.getId())
                .setSubject(userDetails.getLogin())
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private String generateSecretKey(UserDetailsImpl user) {
        StringBuilder secretKey = new StringBuilder();
        secretKey
                .append(user.getUsername())
                .append(user.getLogin())
                .append(user.getPassword());
        return secretKey.toString();
    }

    public void updateUserTokenByLogin(String login, String token) {
        userService.updateUserTokenByLogin(login, token);
    }
}
