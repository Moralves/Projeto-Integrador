import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RelatorioService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/relatorios';

  relatorioOcorrencias(): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/ocorrencias`);
  }
}
