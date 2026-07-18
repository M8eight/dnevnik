import Keycloak from 'keycloak-js'

export const keycloak = new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL,
    realm: 'dnevnik-realm',
    clientId: 'dnevnik-frontend',
})