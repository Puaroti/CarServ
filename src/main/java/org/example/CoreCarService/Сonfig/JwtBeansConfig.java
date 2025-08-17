package org.example.CoreCarService.Сonfig;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtBeansConfig {

    @Bean
    public SecretKey jwtSecretKey(@Value("${jwt.secret:VGhpc0lzQVRlc3RTZWNyZXRGb3JKV1QtSFM1MTJfMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=}") String base64Secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    @Bean
    public JwtParser jwtParser(SecretKey jwtSecretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build();
    }

    @Bean
    public JwtBuilder jwtBuilder(SecretKey jwtSecretKey) {
        return Jwts.builder()
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256);
    }
}
