import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';

interface UsuarioAutenticado {
  id?: number | string;
  login?: string;
  nome?: string;
  email?: string;
  role?: string;
  perfil?: string;
  ativo?: boolean;
  token?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/auth';

  login(credenciais: any): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/login`, credenciais).pipe(
      map(response => {
        const user = this.normalizarUsuarioAutenticado(response);

        return {
          ...response,
          user
        };
      }),
      tap(response => {
        if (response && response.user) {
          localStorage.setItem('user', JSON.stringify(response.user));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('user');
  }

  getUsuarioLogado(): any {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      return null;
    }

    try {
      return this.normalizarUsuarioAutenticado(JSON.parse(userStr));
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('user');
  }

  private normalizarUsuarioAutenticado(usuario: UsuarioAutenticado | null | undefined): UsuarioAutenticado | null {
    if (!usuario) {
      return null;
    }

    const role = usuario.role ?? usuario.perfil;
    const perfil = usuario.perfil ?? usuario.role;

    return {
      ...usuario,
      role,
      perfil
    };
  }
}
