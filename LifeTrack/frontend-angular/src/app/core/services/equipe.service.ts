import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Equipe } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class EquipeService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/equipes';

  listarEquipes(): Observable<Equipe[]> {
    return this.http.get<Equipe[]>(this.API_URL);
  }

  listarEquipesDisponiveis(): Observable<Equipe[]> {
    return this.http.get<Equipe[]>(`${this.API_URL}/disponiveis`);
  }

  criarEquipe(dados: Equipe): Observable<Equipe> {
    return this.http.post<Equipe>(this.API_URL, dados);
  }

  atualizarEquipe(id: number, dados: Equipe): Observable<Equipe> {
    return this.http.put<Equipe>(`${this.API_URL}/${id}`, dados);
  }

  verificarStatus(id: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}/status`);
  }
}
