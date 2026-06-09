import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RelatorioService } from '../../../core/services/relatorio.service';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule],
  providers: [DatePipe],
  templateUrl: './relatorios.component.html',
  styleUrls: ['../admin.shared.css']
})
export class RelatoriosComponent implements OnInit {
  dados: any[] = [];
  loading = true;
  error = '';

  private relatorioService = inject(RelatorioService);
  private datePipe = inject(DatePipe);

  ngOnInit() { this.carregarRelatorio(); }

  carregarRelatorio() {
    this.loading = true;
    this.relatorioService.relatorioOcorrencias().subscribe({
      next: (d) => { this.dados = d; this.error = ''; this.loading = false; },
      error: (e) => { this.error = 'Erro ao carregar relatório: ' + (e.message || e); this.loading = false; }
    });
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

  getGravidadeConfig(g: string): { bg: string; color: string } {
    const map: Record<string, { bg: string; color: string }> = {
      BAIXA: { bg: '#d1fae5', color: '#065f46' },
      MEDIA: { bg: '#fef3c7', color: '#92400e' },
      ALTA:  { bg: '#fee2e2', color: '#dc2626' }
    };
    return map[g] || { bg: '#f3f4f6', color: '#374151' };
  }
}
