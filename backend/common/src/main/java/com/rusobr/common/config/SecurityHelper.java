package com.rusobr.common.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SecurityHelper {
    public static Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakRoleJwtConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Optional.ofNullable(jwt.getClaimAsMap("realm_access"))
                    .map(m -> m.get("roles"))
                    .filter(List.class::isInstance)
                    .map(roles -> (List<?>) roles)
                    .ifPresent(roles -> roles.forEach(r ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + r))));

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}
