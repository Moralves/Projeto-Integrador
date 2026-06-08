import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class HistoricoService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private readonly API_URL = 'http://localhost:8081/api/historico-ocorrencias';

  buscarPorOcorrencia(ocorrenciaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/ocorrencia/${ocorrenciaId}`);
  }

  buscarMeuHistorico(): Observable<any[]> {
    const user = this.authService.getUsuarioLogado();
    if (!user) {
      throw new Error('Usuário não autenticado');
    }
    
    const headers = new HttpHeaders().set('X-User-Id', user.id.toString());
    return this.http.get<any[]>(`${this.API_URL}/meu-historico`, { headers });
  }
}
