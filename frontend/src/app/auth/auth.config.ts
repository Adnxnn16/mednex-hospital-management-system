import { AuthConfig } from 'angular-oauth2-oidc';

export const keycloakIssuer = 'http://localhost:8080/realms/mednex';

export const oauthConfig: AuthConfig = {
  issuer: keycloakIssuer,
  clientId: 'mednex-frontend',
  responseType: 'code',
  redirectUri: window.location.origin + '/auth/callback',
  scope: 'openid profile email',
  requireHttps: false,
  strictDiscoveryDocumentValidation: false,
  showDebugInformation: true
};