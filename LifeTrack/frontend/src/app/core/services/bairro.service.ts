import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BairroService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/bairros';

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.API_URL);
  }
}
