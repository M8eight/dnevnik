package com.rusobr.academic.config.security;

import com.rusobr.academic.infrastructure.client.UserClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentSecurityTest {

    @Mock
    private UserClient userClient;

    @Test
    @DisplayName("isChild делегирует в userClient")
    void isChild_delegatesToUserClient() {
        ParentSecurity parentSecurity = new ParentSecurity(userClient);
        when(userClient.isChild(1L, 2L)).thenReturn(true);

        boolean result = parentSecurity.isChild(1L, 2L);

        assertThat(result).isTrue();
        verify(userClient).isChild(1L, 2L);
    }
}