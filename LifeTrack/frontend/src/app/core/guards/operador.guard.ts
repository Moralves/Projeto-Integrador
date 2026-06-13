import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const operadorGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const user = authService.getUsuarioLogado();

  if (authService.isAuthenticated() && (user?.role === 'OPERADOR' || user?.perfil === 'OPERADOR')) {
    return true;
  }

  return router.parseUrl('/login');
};
