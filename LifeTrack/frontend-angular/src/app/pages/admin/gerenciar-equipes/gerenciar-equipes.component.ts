import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { EquipeService } from '../../../core/services/equipe.service';
import { AmbulanciaService } from '../../../core/services/ambulancia.service';
import { ProfissionalService } from '../../../core/services/profissional.service';

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
  error = '';
  showModal = false;
  editingEquipe: any = null;
  formData: { idAmbulancia: string; descricao: string; idsProfissionais: string[] } = { idAmbulancia: '', descricao: '', idsProfissionais: [] };

  private equipeService = inject(EquipeService);
  private ambulanciaService = inject(AmbulanciaService);
  private profissionalService = inject(ProfissionalService);

  ngOnInit() { this.carregarDados(); }

  carregarDados(incluirTodos = false) {
    this.loading = true;
    forkJoin({
      equipes: this.equipeService.listarEquipes(),
      ambulancias: this.ambulanciaService.listar(),
      profissionais: incluirTodos ? this.profissionalService.listar() : this.profissionalService.listarDisponiveis()
    }).subscribe({
      next: async ({ equipes, ambulancias, profissionais }) => {
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

        // Verificar status de cada equipe
        const statusMap: Record<number, boolean> = {};
        for (const eq of equipes) {
          try {
            const emAtendimento = await this.equipeService.verificarStatus(eq.id).toPromise();
            statusMap[eq.id] = !!emAtendimento;
          } catch { statusMap[eq.id] = false; }
        }
        this.equipesStatus = statusMap;
        this.error = '';
        this.loading = false;
      },
      error: (e) => { this.error = 'Erro ao carregar dados: ' + (e.message || e); this.loading = false; }
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
    this.error = '';
    this.showModal = true;
  }

  openNewModal() {
    this.editingEquipe = null;
    this.formData = { idAmbulancia: '', descricao: '', idsProfissionais: [] };
    this.error = '';
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
    this.error = '';
    const payload: any = {
      descricao: this.formData.descricao,
      idsProfissionais: this.formData.idsProfissionais.map(id => parseInt(id))
    };
    const obs = this.editingEquipe
      ? this.equipeService.atualizarEquipe(this.editingEquipe.id, payload)
      : (payload.idAmbulancia = parseInt(this.formData.idAmbulancia), this.equipeService.criarEquipe(payload));
    obs.subscribe({
      next: () => { this.closeModal(); this.carregarDados(); },
      error: (e) => { this.error = 'Erro: ' + (e.error?.message || e.message); }
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
