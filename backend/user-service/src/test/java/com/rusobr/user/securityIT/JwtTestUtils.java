package com.rusobr.user.securityIT;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Tag;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag("integration")
public final class JwtTestUtils {

    public static final String ISSUER = "http://localhost:9090/realms/dnevnik-realm";

    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final RSAKey RSA_KEY = new RSAKey.Builder((RSAPublicKey) KEY_PAIR.getPublic())
            .privateKey(KEY_PAIR.getPrivate())
            .build();
    private static final RSAPublicKey PUBLIC_KEY = (RSAPublicKey) KEY_PAIR.getPublic();
    private static final JWKSource<SecurityContext> JWK_SOURCE =
            new ImmutableJWKSet<>(new JWKSet(RSA_KEY));

    private JwtTestUtils() {
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate test RSA key", e);
        }
    }

    public static RSAPublicKey publicKey() {
        return PUBLIC_KEY;
    }

    public static String token(Long userId, List<String> roles) {
        return token(userId, roles, Duration.ofHours(1));
    }

    public static String token(Long userId, List<String> roles, Duration validity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("realm_access", Map.of("roles", roles));
        return token(claims, validity);
    }

    public static String token(Map<String, Object> claims, Duration validity) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(validity))
                .subject(String.valueOf(claims.getOrDefault("user_id", "unknown")))
                .claims(c -> c.putAll(claims))
                .build();
        return sign(claimsSet);
    }

    public static String expiredToken(Long userId, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now.minus(Duration.ofHours(2)))
                .expiresAt(now.minus(Duration.ofHours(1)))
                .subject(String.valueOf(userId))
                .claim("user_id", userId)
                .claim("realm_access", Map.of("roles", roles))
                .build();
        return sign(claimsSet);
    }

    private static String sign(JwtClaimsSet claimsSet) {
        return new NimbusJwtEncoder(JWK_SOURCE)
                .encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue();
    }

}
