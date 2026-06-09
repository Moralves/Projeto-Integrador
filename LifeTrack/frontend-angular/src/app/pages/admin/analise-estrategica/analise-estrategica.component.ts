import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnaliseEstrategicaService } from '../../../core/services/analise-estrategica.service';

@Component({
  selector: 'app-analise-estrategica',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analise-estrategica.component.html',
  styleUrls: ['../admin.shared.css']
})
export class AnaliseEstrategicaComponent implements OnInit {
  bairrosSugeridos: any[] = [];
  loading = true;
  error = '';

  private analiseService = inject(AnaliseEstrategicaService);

  ngOnInit() { this.carregarAnalise(); }

  carregarAnalise() {
    this.loading = true;
    this.analiseService.obterBairrosSugeridos(null).subscribe({
      next: (d) => { this.bairrosSugeridos = d; this.error = ''; this.loading = false; },
      error: (e) => { this.error = 'Erro ao carregar análise: ' + (e.message || e); this.loading = false; }
    });
  }

  getPrioridadeBadge(index: number): { bg: string; color: string; text: string } {
    if (index === 0) return { bg: '#fef3c7', color: '#92400e', text: '🥇 Alta Prioridade' };
    if (index === 1) return { bg: '#e0e7ff', color: '#3730a3', text: '🥈 Média Prioridade' };
    if (index === 2) return { bg: '#d1fae5', color: '#065f46', text: '🥉 Boa Opção' };
    return { bg: '#f3f4f6', color: '#374151', text: 'Considerar' };
  }

  getConexoesBadge(n: number): { bg: string; color: string } {
    if (n >= 5) return { bg: '#d1fae5', color: '#065f46' };
    if (n >= 3) return { bg: '#fef3c7', color: '#92400e' };
    return { bg: '#fee2e2', color: '#991b1b' };
  }
}
