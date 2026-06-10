import { Component, OnInit, OnChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfissionalService } from '../../../core/services/profissional.service';

@Component({
  selector: 'app-gerenciar-funcionarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gerenciar-funcionarios.component.html',
  styleUrls: ['../admin.shared.css']
})
export class GerenciarFuncionariosComponent implements OnInit {
  profissionais: any[] = [];
  loading = true;
  error = '';
  showModal = false;
  editingId: number | null = null;
  filtroTurno = '';
  filtroStatus = '';
  formData = { nome: '', funcao: 'MEDICO', contato: '', turno: 'MANHA', status: 'DISPONIVEL', ativo: true };

  private profissionalService = inject(ProfissionalService);

  ngOnInit() { this.carregarProfissionais(); }

  carregarProfissionais() {
    this.loading = true;
    this.profissionalService.listar(this.filtroTurno || null, this.filtroStatus || null).subscribe({
      next: (d) => { this.profissionais = d; this.error = ''; this.loading = false; },
      error: (e) => { this.error = 'Erro ao carregar: ' + (e.message || e); this.loading = false; }
    });
  }

  handleEdit(id: number) {
    this.profissionalService.buscarPorId(id).subscribe({
      next: (prof: any) => {
        if (prof.status === 'EM_ATENDIMENTO') { this.error = 'Não é possível editar funcionário em atendimento.'; return; }
        this.formData = { nome: prof.nome, funcao: prof.funcao, contato: prof.contato || '', turno: prof.turno || 'MANHA', status: prof.status || 'DISPONIVEL', ativo: prof.ativo ?? true };
        this.editingId = id;
        this.error = '';
        this.showModal = true;
      },
      error: (e) => { this.error = 'Erro ao carregar: ' + (e.message || e); }
    });
  }

  openNew() {
    this.editingId = null;
    this.formData = { nome: '', funcao: 'MEDICO', contato: '', turno: 'MANHA', status: 'DISPONIVEL', ativo: true };
    this.error = '';
    this.showModal = true;
  }

  closeModal() { this.showModal = false; this.editingId = null; }

  handleSubmit() {
    this.error = '';
    const obs = this.editingId
      ? this.profissionalService.editar(this.editingId, this.formData as any)
      : this.profissionalService.cadastrar(this.formData as any);
    obs.subscribe({
      next: () => { this.closeModal(); this.carregarProfissionais(); },
      error: (e) => { this.error = 'Erro ao salvar: ' + (e.error?.message || e.message); }
    });
  }

  alterarStatus(id: number, novoStatus: string) {
    this.profissionalService.alterarStatus(id, novoStatus).subscribe({
      next: () => this.carregarProfissionais(),
      error: (e) => { this.error = 'Erro ao alterar status: ' + (e.error?.message || e.message); }
    });
  }

  desativar(id: number) {
    if (!window.confirm('Tem certeza que deseja desativar este profissional?')) return;
    this.profissionalService.desativar(id).subscribe({
      next: () => this.carregarProfissionais(),
      error: (e) => { this.error = 'Erro ao desativar: ' + (e.error?.message || e.message); }
    });
  }

  getStatusBadgeClass(status: string): string {
    const map: Record<string, string> = { DISPONIVEL: 'badge badge-ativo', EM_ATENDIMENTO: 'badge badge-warning', EM_FOLGA: 'badge badge-info', INATIVO: 'badge badge-inativo' };
    return map[status] || 'badge';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = { DISPONIVEL: 'Disponível', EM_ATENDIMENTO: 'Em Atendimento', EM_FOLGA: 'Em Folga', INATIVO: 'Inativo' };
    return map[status] || status;
  }

  getTurnoLabel(turno: string): string {
    const map: Record<string, string> = { MANHA: 'Manhã', TARDE: 'Tarde', NOITE: 'Noite' };
    return map[turno] || turno;
  }
}
