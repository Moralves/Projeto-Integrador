import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ambulancia } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AmbulanciaService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/ambulancias';

  listar(): Observable<Ambulancia[]> {
    return this.http.get<Ambulancia[]>(this.API_URL);
  }

  cadastrar(dados: Ambulancia): Observable<Ambulancia> {
    return this.http.post<Ambulancia>(this.API_URL, dados);
  }

  atualizar(id: number, dados: Ambulancia): Observable<Ambulancia> {
    return this.http.put<Ambulancia>(`${this.API_URL}/${id}`, dados);
  }

  ativar(id: number): Observable<Ambulancia> {
    return this.http.put<Ambulancia>(`${this.API_URL}/${id}/ativar`, {});
  }

  desativar(id: number): Observable<Ambulancia> {
    return this.http.put<Ambulancia>(`${this.API_URL}/${id}/desativar`, {});
  }
}
