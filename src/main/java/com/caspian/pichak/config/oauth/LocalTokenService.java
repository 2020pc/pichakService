package com.caspian.pichak.config.oauth;

import com.caspian.pichak.model.dto.ISOMessageDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class LocalTokenService {

    @Value("${security.local-jwt.secret}")
    private String secret;

    @Value("${security.local-jwt.expiration-ms:3600000}")
    private long expirationMs;

    public String createToken(ISOMessageDTO.User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.getCustomerId())
                .claim("nationalNumber", user.getNationalCode())
                .claim("shahabCode", user.getShahabCode())
                .claim("source", "SOCKET")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}