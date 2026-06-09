import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { OcorrenciaService } from '../../../core/services/ocorrencia.service';
import { SlaTimerComponent } from '../../../shared/components/sla-timer/sla-timer.component';
import { HistoricoOcorrenciaComponent } from '../../../shared/components/historico-ocorrencia/historico-ocorrencia.component';
import { SugerirAmbulanciasComponent } from '../sugerir-ambulancias/sugerir-ambulancias.component';

@Component({
  selector: 'app-listar-ocorrencias',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SlaTimerComponent,
    HistoricoOcorrenciaComponent,
    SugerirAmbulanciasComponent
  ],
  providers: [DatePipe],
  templateUrl: './listar-ocorrencias.component.html',
  styleUrls: ['./listar-ocorrencias.component.css']
})
export class ListarOcorrenciasComponent implements OnInit, OnDestroy {
  ocorrencias: any[] = [];
  loading = true;
  error = '';
  filtroStatus = 'TODAS';
  filtroGravidade = 'TODAS';
  busca = '';
  despachando: number | null = null;
  mostrarSugestoes: number | null = null;

  private ocorrenciaService = inject(OcorrenciaService);
  private datePipe = inject(DatePipe);
  private refreshSubscription?: Subscription;

  ngOnInit() {
    this.carregarOcorrencias();
    this.refreshSubscription = interval(5000).subscribe(() => this.carregarOcorrencias());
  }

  ngOnDestroy() {
    this.refreshSubscription?.unsubscribe();
  }

  carregarOcorrencias() {
    this.ocorrenciaService.listar().subscribe({
      next: (dados) => {
        this.ocorrencias = dados;
        this.error = '';
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar ocorrências: ' + (err.message || err);
        this.loading = false;
      }
    });
  }

  get ocorrenciasFiltradas(): any[] {
    return this.ocorrencias.filter(oc => {
      const matchStatus = this.filtroStatus === 'TODAS' || oc.status === this.filtroStatus;
      const matchGravidade = this.filtroGravidade === 'TODAS' || oc.gravidade === this.filtroGravidade;
      const matchBusca = !this.busca ||
        oc.tipoOcorrencia?.toLowerCase().includes(this.busca.toLowerCase()) ||
        oc.bairroLocal?.nome?.toLowerCase().includes(this.busca.toLowerCase());
      return matchStatus && matchGravidade && matchBusca;
    });
  }

  handleDespachar(id: number) {
    if (!window.confirm('Deseja realmente despachar esta ocorrência?')) return;
    this.despachando = id;
    this.error = '';
    this.ocorrenciaService.despachar(id).subscribe({
      next: () => {
        alert('Ocorrência despachada com sucesso!');
        this.carregarOcorrencias();
      },
      error: (err) => {
        this.error = 'Erro ao despachar: ' + (err.error?.message || err.message);
        alert('Erro ao despachar: ' + (err.error?.message || err.message));
      },
      complete: () => (this.despachando = null)
    });
  }

  onDespacharAposSugestao() {
    this.mostrarSugestoes = null;
    this.carregarOcorrencias();
  }

  formatarData(data: any): string {
    return this.datePipe.transform(data, 'dd/MM/yyyy HH:mm') || '-';
  }

  getStatusConfig(status: string): { bg: string; color: string; text: string } {
    const map: Record<string, { bg: string; color: string; text: string }> = {
      ABERTA:         { bg: '#d1fae5', color: '#065f46', text: '🟢 ABERTA' },
      DESPACHADA:     { bg: '#dbeafe', color: '#1e40af', text: '🔵 DESPACHADA' },
      EM_ATENDIMENTO: { bg: '#fef3c7', color: '#92400e', text: '🟡 EM ATENDIMENTO' },
      CONCLUIDA:      { bg: '#e0e7ff', color: '#3730a3', text: '✅ CONCLUÍDA' },
      CANCELADA:      { bg: '#fee2e2', color: '#991b1b', text: '🔴 CANCELADA' }
    };
    return map[status] || { bg: '#f3f4f6', color: '#374151', text: status };
  }

  getGravidadeConfig(gravidade: string): { bg: string; color: string } {
    const map: Record<string, { bg: string; color: string }> = {
      BAIXA: { bg: '#d1fae5', color: '#065f46' },
      MEDIA: { bg: '#fef3c7', color: '#92400e' },
      ALTA:  { bg: '#fee2e2', color: '#dc2626' }
    };
    return map[gravidade] || { bg: '#f3f4f6', color: '#374151' };
  }

  showSlaTimer(status: string): boolean {
    return ['ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA'].includes(status);
  }

  showHistorico(status: string): boolean {
    return ['DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA'].includes(status);
  }
}
