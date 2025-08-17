package org.example.CoreCarService.Сonfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {

    private final JwtParser jwtParser;
    private final io.jsonwebtoken.JwtBuilder jwtBuilder;
    private final long expirationSeconds;

    public JwtTokenService(JwtParser jwtParser,
                           io.jsonwebtoken.JwtBuilder jwtBuilder,
                           @Value("${jwt.expirationSeconds:3600}") long expirationSeconds) {
        this.jwtParser = jwtParser;
        this.jwtBuilder = jwtBuilder;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        return jwtBuilder
                .setSubject(username)
                .addClaims(Map.of("role", role))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .compact();
    }

    public Claims parse(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }
}
