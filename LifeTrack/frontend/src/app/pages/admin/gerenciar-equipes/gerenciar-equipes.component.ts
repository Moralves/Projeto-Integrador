import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom, forkJoin } from 'rxjs';
import { EquipeService } from '../../../core/services/equipe.service';
import { AmbulanciaService } from '../../../core/services/ambulancia.service';
import { ProfissionalService } from '../../../core/services/profissional.service';
import { renderChanges } from '../../../core/utils/render-changes';

@Component({
  selector: 'app-gerenciar-equipes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gerenciar-equipes.component.html',
  styleUrls: ['../admin.shared.css']
})
export class GerenciarEquipesComponent implements OnInit {
  equipes: any[] = [];
  equipesStatus: Record<number, boolean> = {};
  ambulancias: any[] = [];
  profissionais: any[] = [];
  loading = true;
  pageError = '';
  modalError = '';
  showModal = false;
  editingEquipe: any = null;
  formData: { idAmbulancia: string; descricao: string; idsProfissionais: string[] } = { idAmbulancia: '', descricao: '', idsProfissionais: [] };

  private equipeService = inject(EquipeService);
  private ambulanciaService = inject(AmbulanciaService);
  private profissionalService = inject(ProfissionalService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit() { this.carregarDados(); }

  carregarDados(incluirTodos = false) {
    this.loading = true;
    forkJoin({
      equipes: this.equipeService.listarEquipes(),
      ambulancias: this.ambulanciaService.listar(),
      profissionais: incluirTodos ? this.profissionalService.listar() : this.profissionalService.listarDisponiveis()
    }).subscribe({
      next: async ({ equipes, ambulancias, profissionais }) => {
        try {
          this.equipes = equipes;
          this.ambulancias = ambulancias;

          // Filtrar profissionais ativos
          let filtrados = profissionais.filter((p: any) => p.ativo);
          if (!incluirTodos) {
            const emEquipes = new Set<number>();
            equipes.forEach((eq: any) => {
              if (eq.ativa && eq.profissionais) {
                eq.profissionais.forEach((ep: any) => { if (ep.profissional?.id) emEquipes.add(ep.profissional.id); });
              }
            });
            filtrados = filtrados.filter((p: any) => !emEquipes.has(p.id));
          }
          this.profissionais = filtrados;

          const statusEntries = await Promise.all(
            equipes.map(async (eq: any) => {
              try {
                const emAtendimento = await firstValueFrom(this.equipeService.verificarStatus(eq.id as number));
                return [eq.id as number, !!emAtendimento] as const;
              } catch {
                return [eq.id as number, false] as const;
              }
            })
          );

          this.equipesStatus = Object.fromEntries(statusEntries);
          this.pageError = '';
        } catch (e: any) {
          this.pageError = 'Erro ao carregar dados: ' + (e?.message || e);
        } finally {
          this.loading = false;
          renderChanges(this.cdr);
        }
      },
      error: (e) => { this.pageError = 'Erro ao carregar dados: ' + (e.message || e); this.loading = false; renderChanges(this.cdr); }
    });
  }

  async openEditModal(equipe: any) {
    this.editingEquipe = equipe;
    this.formData = {
      idAmbulancia: equipe.ambulancia?.id?.toString() || '',
      descricao: equipe.descricao || '',
      idsProfissionais: equipe.profissionais?.map((ep: any) => ep.profissional?.id?.toString()).filter(Boolean) || []
    };
    this.carregarDados(true);
    this.modalError = '';
    this.showModal = true;
  }

  openNewModal() {
    this.editingEquipe = null;
    this.formData = { idAmbulancia: '', descricao: '', idsProfissionais: [] };
    this.modalError = '';
    this.showModal = true;
  }

  closeModal() { this.showModal = false; this.editingEquipe = null; }

  toggleProfissional(idStr: string) {
    const ids = this.formData.idsProfissionais;
    this.formData.idsProfissionais = ids.includes(idStr) ? ids.filter(i => i !== idStr) : [...ids, idStr];
  }

  isNaEquipe(prof: any): boolean {
    return !!this.editingEquipe?.profissionais?.some((ep: any) => ep.profissional?.id === prof.id);
  }

  podeSelecionar(prof: any): boolean {
    return !this.editingEquipe || prof.status === 'DISPONIVEL' || this.isNaEquipe(prof);
  }

  canEdit(equipe: any): boolean {
    return equipe.ativa && !this.equipesStatus[equipe.id];
  }

  profissionaisText(equipe: any): string {
    return equipe.profissionais?.length > 0
      ? equipe.profissionais.map((ep: any) => ep.profissional?.nome).join(', ')
      : 'Sem profissionais';
  }

  handleSubmit() {
    this.modalError = '';
    const payload: any = {
      descricao: this.formData.descricao,
      idsProfissionais: this.formData.idsProfissionais.map(id => parseInt(id))
    };
    const obs = this.editingEquipe
      ? this.equipeService.atualizarEquipe(this.editingEquipe.id, payload)
      : (payload.idAmbulancia = parseInt(this.formData.idAmbulancia), this.equipeService.criarEquipe(payload));
    obs.pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: () => { this.closeModal(); this.carregarDados(); },
      error: (e) => { this.modalError = 'Erro: ' + (e.error?.message || e.message); }
    });
  }

  getFuncaoColor(funcao: string): string {
    const f = funcao?.toUpperCase() || '';
    if (f.includes('MEDICO')) return '#dc3545';
    if (f.includes('ENFERMEIRO')) return '#17a2b8';
    if (f.includes('CONDUTOR')) return '#28a745';
    return '#6c757d';
  }

  getTurnoColor(turno: string): string {
    const t = turno?.toUpperCase() || '';
    if (t.includes('MANHA')) return '#ffc107';
    if (t.includes('TARDE')) return '#fd7e14';
    if (t.includes('NOITE')) return '#6f42c1';
    return '#6c757d';
  }
}
