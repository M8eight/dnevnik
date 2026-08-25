package com.rusobr.user.securityIT;

import com.rusobr.common.config.SecurityHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class KeycloakRoleConverterTest {

    private final Converter<Jwt, ? extends AbstractAuthenticationToken> converter =
            SecurityHelper.keycloakRoleJwtConverter();

    @Test
    @DisplayName("realm_access.roles -> ROLE_* authorities")
    void shouldMapRealmAccessRolesToRoleAuthorities() {
        Jwt jwt = buildJwt(Map.of("realm_access", Map.of("roles", List.of("TEACHER", "USER"))));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(authorities(token)).containsExactlyInAnyOrder("ROLE_TEACHER", "ROLE_USER");
    }

    @Test
    @DisplayName("Отсутствие realm_access -> пустой набор authorities")
    void shouldReturnEmptyAuthoritiesWhenRealmAccessMissing() {
        Jwt jwt = buildJwt(Map.of("sub", "user-id-123"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(authorities(token)).isEmpty();
    }

    @Test
    @DisplayName("Нестроковые значения ролей отсеиваются")
    void shouldIgnoreNonStringRoles() {
        Jwt jwt = buildJwt(Map.of("realm_access", Map.of("roles", List.of("TEACHER", 123, true))));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(authorities(token)).containsExactly("ROLE_TEACHER");
    }

    @Test
    @DisplayName("resource_access.account не попадает в authorities")
    void shouldNotMapResourceAccessAccountRoles() {
        Jwt jwt = buildJwt(Map.of(
                "realm_access", Map.of("roles", List.of("STUDENT")),
                "resource_access", Map.of("account", Map.of("roles", List.of("manage-account")))));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(authorities(token)).containsExactly("ROLE_STUDENT");
    }

    private Collection<String> authorities(AbstractAuthenticationToken token) {
        return token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .build();
    }

}
