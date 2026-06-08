import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const userStr = localStorage.getItem('user');
  let clonedRequest = req;

  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user && user.id) {
        clonedRequest = req.clone({
          setHeaders: {
            'X-User-Id': user.id.toString()
          }
        });
      }
    } catch (e) {
      console.error('Error parsing user from localStorage', e);
    }
  }

  return next(clonedRequest);
};
