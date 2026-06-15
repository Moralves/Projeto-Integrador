import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { UsuarioService } from '../../../core/services/usuario.service';
import { renderChanges } from '../../../core/utils/render-changes';

@Component({
  selector: 'app-gerenciar-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gerenciar-usuarios.component.html',
  styleUrls: ['../admin.shared.css']
})
export class GerenciarUsuariosComponent implements OnInit {
  usuarios: any[] = [];
  loading = true;
  pageError = '';
  modalError = '';
  showModal = false;
  editingUsuario: any = null;

  formData = { username: '', password: '', nome: '', email: '', telefone: '' };

  private usuarioService = inject(UsuarioService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit() { this.carregarUsuarios(); }

  carregarUsuarios() {
    this.loading = true;
    this.usuarioService.listar().pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: (d) => { this.usuarios = d; this.pageError = ''; this.loading = false; },
      error: (e) => { this.pageError = 'Erro ao carregar usuários: ' + (e.message || e); this.loading = false; }
    });
  }

  openModal(usuario: any = null) {
    this.editingUsuario = usuario;
    this.formData = usuario
      ? { username: usuario.username, password: '', nome: usuario.nome, email: usuario.email, telefone: usuario.telefone || '' }
      : { username: '', password: '', nome: '', email: '', telefone: '' };
    this.modalError = '';
    this.showModal = true;
  }

  closeModal() { this.showModal = false; this.editingUsuario = null; }

  handleSubmit() {
    this.modalError = '';
    if (!this.editingUsuario && !this.formData.password) {
      this.modalError = 'Senha é obrigatória para novos usuários';
      renderChanges(this.cdr);
      return;
    }

    const dados = { ...this.formData };
    
    // Formatar telefone para o padrão esperado pelo backend: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX
    if (dados.telefone) {
      const nums = dados.telefone.replace(/\D/g, '');
      if (nums.length === 11) {
        dados.telefone = `(${nums.substring(0, 2)}) ${nums.substring(2, 7)}-${nums.substring(7)}`;
      } else if (nums.length === 10) {
        dados.telefone = `(${nums.substring(0, 2)}) ${nums.substring(2, 6)}-${nums.substring(6)}`;
      } else {
        this.modalError = 'O telefone deve ter 10 ou 11 dígitos (incluindo DDD)';
        renderChanges(this.cdr);
        return;
      }
    }

    const obs = this.editingUsuario
      ? this.usuarioService.atualizarUsuario(this.editingUsuario.id, dados)
      : this.usuarioService.criarUsuario(dados);
    obs.pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: () => { this.closeModal(); this.carregarUsuarios(); },
      error: (e) => { this.modalError = 'Erro ao salvar: ' + (e.error?.message || e.message); }
    });
  }

  handleDelete(id: number) {
    if (!window.confirm('Tem certeza que deseja deletar este usuário?')) return;
    this.usuarioService.deletarUsuario(id).pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: () => this.carregarUsuarios(),
      error: (e) => { this.pageError = 'Erro ao deletar: ' + (e.error?.message || e.message); }
    });
  }

  handleToggleStatus(id: number) {
    this.usuarioService.toggleStatusUsuario(id).pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: () => this.carregarUsuarios(),
      error: (e) => { this.pageError = 'Erro ao alterar status: ' + (e.error?.message || e.message); }
    });
  }

  rolesText(usuario: any): string {
    return usuario.roles?.length > 0 ? Array.from(usuario.roles as string[]).join(', ') : 'OPERADOR';
  }
}
