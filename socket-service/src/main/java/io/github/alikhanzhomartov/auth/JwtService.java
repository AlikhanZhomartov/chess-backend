package io.github.alikhanzhomartov.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.github.alikhanzhomartov.config.SecurityConfig;


public class JwtService {

    private final JWTVerifier jwtVerifier;

    public JwtService() {
        Algorithm algorithm = Algorithm.HMAC256(SecurityConfig.JWT_SECRET);
        this.jwtVerifier = JWT.require(algorithm).build();
    }

    public DecodedJWT verifyToken(String token) {
        return jwtVerifier.verify(token);
    }
}
