import { Component, Input, OnInit, OnDestroy, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription, interval } from 'rxjs';
import { OcorrenciaService } from '../../../core/services/ocorrencia.service';

@Component({
  selector: 'app-sla-timer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sla-timer.component.html',
  styleUrls: ['./sla-timer.component.css']
})
export class SlaTimerComponent implements OnInit, OnDestroy, OnChanges {
  @Input() ocorrenciaId!: number;
  @Input() status!: string;

  timer: any = null;
  loading: boolean = true;
  error: string | null = null;
  percentual: number = 0;
  etapas: any = { ateDespacho: 0, ateChegada: 0, retorno: 0, total: 0 };

  private ocorrenciaService = inject(OcorrenciaService);
  private timerSubscription?: Subscription;

  // Status que devem mostrar o timer
  private readonly statusComTimer = ['ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA'];

  ngOnInit() {
    this.iniciarTimer();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ocorrenciaId'] || changes['status']) {
      this.iniciarTimer();
    }
  }

  ngOnDestroy() {
    this.pararTimer();
  }

  private iniciarTimer() {
    this.pararTimer();
    
    if (!this.ocorrenciaId || !this.statusComTimer.includes(this.status)) {
      this.loading = false;
      return;
    }

    this.carregarTimer();

    // Atualizar a cada 2 segundos sempre (mesmo quando concluída, para mostrar retorno)
    this.timerSubscription = interval(2000).subscribe(() => {
      this.carregarTimer();
    });
  }

  private pararTimer() {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
      this.timerSubscription = undefined;
    }
  }

  private carregarTimer() {
    this.ocorrenciaService.obterTimer(this.ocorrenciaId).subscribe({
      next: (dados) => {
        this.timer = dados;
        this.error = null;
        this.loading = false;
        this.atualizarCalculos();
      },
      error: (err) => {
        this.error = err.message || 'Erro ao carregar timer';
        this.loading = false;
      }
    });
  }

  private atualizarCalculos() {
    if (!this.timer) return;
    this.percentual = this.calcularPercentualSla();
    this.etapas = this.calcularEtapasProgresso();
  }

  getSlaStatusClass(): string {
    if (this.timer?.slaExcedido) return 'sla-excedido';
    if (this.timer?.slaEmRisco) return 'sla-risco';
    return 'sla-ok';
  }

  getSlaStatusText(): string {
    if (this.timer?.slaExcedido) return 'SLA Excedido';
    if (this.timer?.slaEmRisco) return 'SLA em Risco';
    return 'SLA OK';
  }

  private calcularPercentualSla(): number {
    if (!this.timer?.slaMinutos || this.timer.slaMinutos === 0) return 0;
    const percentual = ((this.timer.tempoSlaDecorridoMinutos || 0) / this.timer.slaMinutos) * 100;
    return Math.min(100, Math.max(0, percentual));
  }

  private calcularEtapasProgresso(): any {
    if (!this.timer?.slaMinutos || this.timer.slaMinutos === 0) {
      return { ateDespacho: 0, ateChegada: 0, retorno: 0, total: 0 };
    }

    const slaTotalMinutos = this.timer.slaMinutos;
    let ateDespachoPercentual = 0;
    let ateChegadaPercentual = 0;
    let retornoPercentual = 0;

    // Calcular tempo até despacho (desde abertura até despacho)
    if (this.timer.foiDespachada && this.timer.dataHoraDespacho && this.timer.dataHoraAbertura) {
      try {
        const tempoAteDespachoSegundos = (new Date(this.timer.dataHoraDespacho).getTime() - new Date(this.timer.dataHoraAbertura).getTime()) / 1000;
        const tempoAteDespachoMinutos = tempoAteDespachoSegundos / 60;
        ateDespachoPercentual = (tempoAteDespachoMinutos / slaTotalMinutos) * 100;
      } catch (e) {
        ateDespachoPercentual = 0;
      }
    }

    // Calcular tempo de ida (despacho até chegada)
    if (this.timer.tempoAteChegadaMinutos !== null && this.timer.tempoAteChegadaMinutos !== undefined) {
      ateChegadaPercentual = (this.timer.tempoAteChegadaMinutos / slaTotalMinutos) * 100;
    }

    // Calcular tempo de retorno (apenas se OS está finalizada)
    if (this.timer.foiConcluida && this.timer.tempoRetornoDecorridoMinutos !== null && this.timer.tempoRetornoDecorridoMinutos !== undefined) {
      retornoPercentual = (this.timer.tempoRetornoDecorridoMinutos / slaTotalMinutos) * 100;
    }

    // Calcular o total usado do SLA (apenas tempo até chegada, sem retorno)
    const tempoSlaUsado = (this.timer.tempoSlaDecorridoMinutos || 0);
    const totalPercentual = (tempoSlaUsado / slaTotalMinutos) * 100;

    // Normalizar para que a soma das etapas visíveis não exceda o total
    const tempoAteChegadaTotal = ateDespachoPercentual + ateChegadaPercentual;

    // Se o tempo usado é diferente do esperado, ajustar proporcionalmente
    if (tempoAteChegadaTotal > 0 && totalPercentual > 0) {
      const fatorAjuste = Math.min(1, totalPercentual / tempoAteChegadaTotal);
      ateDespachoPercentual *= fatorAjuste;
      ateChegadaPercentual *= fatorAjuste;
    }

    return {
      ateDespacho: Math.min(100, Math.max(0, ateDespachoPercentual)),
      ateChegada: Math.min(100, Math.max(0, ateChegadaPercentual)),
      retorno: Math.min(100, Math.max(0, retornoPercentual)),
      total: Math.min(100, Math.max(0, totalPercentual))
    };
  }

  get showTimer(): boolean {
    return !!this.ocorrenciaId && this.statusComTimer.includes(this.status);
  }
}
