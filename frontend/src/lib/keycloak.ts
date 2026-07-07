import Keycloak from 'keycloak-js'

export const keycloak = new Keycloak({
    url: 'http://localhost:9090',
    realm: 'dnevnik-realm',
    clientId: 'dnevnik-frontend',
})