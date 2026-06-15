import { ChangeDetectorRef, Component, Input, OnInit, OnDestroy, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { HistoricoService } from '../../../core/services/historico.service';
import { renderChanges } from '../../../core/utils/render-changes';

@Component({
  selector: 'app-historico-ocorrencia',
  standalone: true,
  imports: [CommonModule],
  providers: [DatePipe],
  templateUrl: './historico-ocorrencia.component.html',
  styleUrls: ['./historico-ocorrencia.component.css']
})
export class HistoricoOcorrenciaComponent implements OnInit, OnDestroy, OnChanges {
  @Input() ocorrenciaId!: number;
  @Input() atualizarEmTempoReal: boolean = false;

  historicos: any[] = [];
  loading: boolean = true;
  error: string | null = null;

  private historicoService = inject(HistoricoService);
  private datePipe = inject(DatePipe);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit() {
    this.iniciarBusca();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ocorrenciaId'] || changes['atualizarEmTempoReal']) {
      this.iniciarBusca();
    }
  }

  ngOnDestroy() {
  }

  private iniciarBusca() {
    if (!this.ocorrenciaId) {
      this.loading = false;
      return;
    }

    this.carregarHistorico();
  }

  private carregarHistorico() {
    this.historicoService.buscarPorOcorrencia(this.ocorrenciaId).pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: (dados) => {
        this.historicos = dados;
        this.error = null;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Erro ao carregar histórico';
        this.loading = false;
      }
    });
  }

  formatarData(data: any): string {
    if (!data) return '-';
    return this.datePipe.transform(data, 'dd/MM/yyyy HH:mm:ss') || '-';
  }

  getAcaoIcon(acao: string): string {
    const icons: { [key: string]: string } = {
      'ABERTURA': '🆕',
      'DESPACHO': '🚑',
      'CHEGADA': '📍',
      'CONCLUSAO': '✅',
      'ALTERACAO_STATUS': '🔄',
      'CANCELAMENTO': '❌'
    };
    return icons[acao] || '📝';
  }

  getAcaoColor(acao: string): string {
    const colors: { [key: string]: string } = {
      'ABERTURA': '#17a2b8',
      'DESPACHO': '#007bff',
      'CHEGADA': '#28a745',
      'CONCLUSAO': '#6f42c1',
      'ALTERACAO_STATUS': '#ffc107',
      'CANCELAMENTO': '#dc3545'
    };
    return colors[acao] || '#6c757d';
  }
}
