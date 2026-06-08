import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Profissional } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ProfissionalService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/profissionais';

  listar(turno?: string | null, status?: string | null): Observable<Profissional[]> {
    let params = new HttpParams();
    if (turno) params = params.set('turno', turno);
    if (status) params = params.set('status', status);
    
    return this.http.get<Profissional[]>(this.API_URL, { params });
  }

  listarDisponiveis(turno?: string | null): Observable<Profissional[]> {
    let params = new HttpParams();
    if (turno) params = params.set('turno', turno);
    
    return this.http.get<Profissional[]>(`${this.API_URL}/disponiveis`, { params });
  }

  buscarPorId(id: number): Observable<Profissional> {
    return this.http.get<Profissional>(`${this.API_URL}/${id}`);
  }

  cadastrar(profissional: Profissional): Observable<Profissional> {
    return this.http.post<Profissional>(this.API_URL, profissional);
  }

  editar(id: number, dados: Profissional): Observable<Profissional> {
    return this.http.put<Profissional>(`${this.API_URL}/${id}`, dados);
  }

  alterarStatus(id: number, status: string): Observable<Profissional> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Profissional>(`${this.API_URL}/${id}/status`, {}, { params });
  }

  desativar(id: number): Observable<Profissional> {
    return this.http.put<Profissional>(`${this.API_URL}/${id}/desativar`, {});
  }
}
