import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';
import { AnaliseEstrategicaService } from '../../../core/services/analise-estrategica.service';
import { renderChanges } from '../../../core/utils/render-changes';

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
  private cdr = inject(ChangeDetectorRef);

  ngOnInit() { this.carregarAnalise(); }

  carregarAnalise() {
    this.loading = true;
    this.analiseService.obterBairrosSugeridos(null).pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
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

  getJustificativaPonto(bairro: any): string {
    const conexoes = bairro.bairrosAlcancaveis || 0;
    const ocorrencias = bairro.ocorrenciasNoBairro || 0;
    const ambulancias = bairro.ambulanciasExistentes || 0;
    const tempoMedio = bairro.tempoMedioResposta || 0;
    const textoConexoes = conexoes === 1 ? '1 conexão' : `${conexoes} conexões`;
    const textoOcorrencias = ocorrencias === 1 ? '1 ocorrência' : `${ocorrencias} ocorrências`;
    const criterios: string[] = [];

    if (conexoes >= 5) {
      criterios.push(`alta conectividade (${textoConexoes})`);
    } else if (conexoes >= 3) {
      criterios.push(`boa conectividade (${textoConexoes})`);
    } else if (conexoes > 0) {
      criterios.push(`posição estratégica na rede (${textoConexoes})`);
    }

    if (ocorrencias >= 5) {
      criterios.push(`alta demanda (${textoOcorrencias})`);
    } else if (ocorrencias > 0) {
      criterios.push(`demanda registrada (${textoOcorrencias})`);
    }

    if (ambulancias === 0) {
      criterios.push('ausência de ambulâncias no bairro');
    } else if (ambulancias === 1) {
      criterios.push('cobertura limitada');
    }

    if (tempoMedio > 0) {
      criterios.push(`tempo médio de ${tempoMedio.toFixed(1)} min`);
    }

    if (criterios.length === 0) {
      return 'Ponto indicado para ampliar a cobertura estratégica do atendimento.';
    }

    return `Ponto recomendado por ${this.formatarLista(criterios)}.`;
  }

  private formatarLista(itens: string[]): string {
    if (itens.length <= 1) return itens[0] || '';
    return `${itens.slice(0, -1).join(', ')} e ${itens[itens.length - 1]}`;
  }
}
