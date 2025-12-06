import { ApplicationConfig, ErrorHandler, importProvidersFrom } from '@angular/core';
import { provideZoneChangeDetection } from '@angular/core'; 
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth-interceptor'; 
import { routes } from './app.routes'; 
import { SocialLoginModule, SocialAuthServiceConfig, SocialAuthService, SOCIAL_AUTH_CONFIG } from '@abacritt/angularx-social-login';
import { GoogleLoginProvider } from '@abacritt/angularx-social-login';

import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export function provideSocialAuthConfig(http: HttpClient) {
  return firstValueFrom(http.get<any>('http://localhost:8080/api/auth/config')).then(config => {
    console.log('Received Auth Config:', config); // Debug Log
    if (!config || !config.googleClientId) {
        console.error('Google Client ID is missing from backend response!');
    }
    return {
      autoLogin: false,
      providers: [
        {
          id: GoogleLoginProvider.PROVIDER_ID,
          provider: new GoogleLoginProvider(config.googleClientId)
        }
      ],
      onError: (err) => {
        console.error(err);
      }
    } as SocialAuthServiceConfig;
  }).catch(err => {
    console.error('Failed to load auth config', err);
    throw err;
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
        withInterceptors([authInterceptor]) 
    ),
    importProvidersFrom(SocialLoginModule),
    { provide: ErrorHandler, useClass: ErrorHandler },
    {
      provide: SOCIAL_AUTH_CONFIG,
      useFactory: provideSocialAuthConfig,
      deps: [HttpClient]
    }
  ]
};
