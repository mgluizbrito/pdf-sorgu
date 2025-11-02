package io.github.mgluizbrito.PdfSorgu.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.github.mgluizbrito.PdfSorgu.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Service
public class TokenService {

    @Value("${api.security.jwt.secret:7kM/cuJAE45bghl/XETa2VgxTX9LLyxm5Ez9N2MNx5g=}")
    private String secret;

    @Value("${api.security.jwt.expiration-seconds:604800}")
    private long expiration;

    public String generateToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String scope = user.getRoles().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(" "));

            return JWT.create()
                    .withIssuer("pdf-sorgu-api")
                    .withIssuedAt(Instant.now())
                    .withSubject(user.getEmail())
                    .withExpiresAt(this.getExpirationDate())
                    .withClaim("scope", scope)
                    .sign(algorithm);

        }catch (JWTCreationException e){
            throw new RuntimeException("error while authenticating: "+e.getMessage());
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("pdf-sorgu-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException e){
            return null;
        }
    }

    private Instant getExpirationDate() {
        return LocalDateTime.now().plusSeconds(this.expiration).toInstant(ZoneOffset.of("-03:00"));
    }
}
