import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AnaliseEstrategicaService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/api/bairros/sugeridos';

  obterBairrosSugeridos(tipoAmbulancia?: string | null): Observable<any[]> {
    let params = new HttpParams();
    if (tipoAmbulancia) {
      params = params.set('tipoAmbulancia', tipoAmbulancia);
    }
    return this.http.get<any[]>(this.API_URL, { params });
  }
}
