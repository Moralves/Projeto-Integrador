import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OcorrenciaService } from '../../../core/services/ocorrencia.service';

@Component({
  selector: 'app-sugerir-ambulancias',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sugerir-ambulancias.component.html',
  styleUrls: ['./sugerir-ambulancias.component.css']
})
export class SugerirAmbulanciasComponent implements OnInit, OnChanges {
  @Input() ocorrenciaId!: number;
  @Output() despachar = new EventEmitter<void>();
  @Output() fechar = new EventEmitter<void>();

  sugestoes: any[] = [];
  loading = true;
  error = '';
  despachando = false;

  private ocorrenciaService = inject(OcorrenciaService);

  ngOnInit() {
    this.carregarSugestoes();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ocorrenciaId'] && !changes['ocorrenciaId'].firstChange) {
      this.carregarSugestoes();
    }
  }

  carregarSugestoes() {
    this.loading = true;
    this.error = '';
    this.ocorrenciaService.sugerirAmbulancias(this.ocorrenciaId).subscribe({
      next: (dados) => {
        this.sugestoes = dados;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar sugestões: ' + (err.message || err);
        this.loading = false;
      }
    });
  }

  handleDespachar() {
    if (!window.confirm('Deseja realmente despachar esta ocorrência? O sistema escolherá automaticamente a melhor ambulância disponível.')) return;
    this.despachando = true;
    this.ocorrenciaService.despachar(this.ocorrenciaId).subscribe({
      next: () => {
        alert('Ocorrência despachada com sucesso!');
        this.despachar.emit();
        this.fechar.emit();
      },
      error: (err) => {
        const msg = err.error?.message || err.message || 'Erro desconhecido';
        this.error = 'Erro ao despachar: ' + msg;
        alert('Erro ao despachar: ' + msg);
        this.despachando = false;
      }
    });
  }

  getSlaBadge(dentroSLA: boolean, slaMinutos: number) {
    return dentroSLA
      ? { bg: '#d1fae5', color: '#065f46', text: `✅ Dentro do SLA (${slaMinutos} min)` }
      : { bg: '#fee2e2', color: '#991b1b', text: `⚠️ Fora do SLA (${slaMinutos} min)` };
  }

  getTipoBadge(tipo: string) {
    const map: Record<string, { bg: string; color: string; text: string }> = {
      BASICA: { bg: '#dbeafe', color: '#1e40af', text: '🚑 Básica' },
      UTI:    { bg: '#fee2e2', color: '#dc2626', text: '🚨 UTI' }
    };
    return map[tipo] || { bg: '#f3f4f6', color: '#374151', text: tipo };
  }
}
