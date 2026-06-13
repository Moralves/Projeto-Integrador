import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, timeout, throwError } from 'rxjs';

const REQUEST_TIMEOUT_MS = 15000;

export const requestTimeoutInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    timeout(REQUEST_TIMEOUT_MS),
    catchError((err) => {
      if (err?.name === 'TimeoutError') {
        return throwError(() => new Error(`Tempo limite excedido ao acessar ${req.url}`));
      }

      return throwError(() => err);
    })
  );
};