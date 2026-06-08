import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ocorrencia } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class OcorrenciaService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/ocorrencias';

  listar(): Observable<Ocorrencia[]> {
    return this.http.get<Ocorrencia[]>(this.API_URL);
  }

  registrar(dados: Ocorrencia): Observable<Ocorrencia> {
    return this.http.post<Ocorrencia>(this.API_URL, dados);
  }

  despachar(idOcorrencia: number): Observable<any> {
    return this.http.post(`${this.API_URL}/${idOcorrencia}/despachar`, {});
  }

  buscarPorId(id: number): Observable<Ocorrencia> {
    return this.http.get<Ocorrencia>(`${this.API_URL}/${id}`);
  }

  sugerirAmbulancias(idOcorrencia: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/${idOcorrencia}/ambulancias-sugeridas`);
  }

  obterTimer(idOcorrencia: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${idOcorrencia}/timer`);
  }

  registrarRetorno(idAtendimento: number): Observable<any> {
    return this.http.post(`${this.API_URL}/atendimentos/${idAtendimento}/retorno`, {});
  }
}
