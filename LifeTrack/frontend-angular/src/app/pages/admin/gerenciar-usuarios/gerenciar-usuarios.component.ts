import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../../core/services/usuario.service';

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
  error = '';
  showModal = false;
  editingUsuario: any = null;

  formData = { username: '', password: '', nome: '', email: '', telefone: '' };

  private usuarioService = inject(UsuarioService);

  ngOnInit() { this.carregarUsuarios(); }

  carregarUsuarios() {
    this.loading = true;
    this.usuarioService.listar().subscribe({
      next: (d) => { this.usuarios = d; this.error = ''; this.loading = false; },
      error: (e) => { this.error = 'Erro ao carregar usuários: ' + (e.message || e); this.loading = false; }
    });
  }

  openModal(usuario: any = null) {
    this.editingUsuario = usuario;
    this.formData = usuario
      ? { username: usuario.username, password: '', nome: usuario.nome, email: usuario.email, telefone: usuario.telefone || '' }
      : { username: '', password: '', nome: '', email: '', telefone: '' };
    this.error = '';
    this.showModal = true;
  }

  closeModal() { this.showModal = false; this.editingUsuario = null; }

  handleSubmit() {
    this.error = '';
    if (!this.editingUsuario && !this.formData.password) {
      this.error = 'Senha é obrigatória para novos usuários';
      return;
    }
    const obs = this.editingUsuario
      ? this.usuarioService.atualizarUsuario(this.editingUsuario.id, this.formData)
      : this.usuarioService.criarUsuario(this.formData);
    obs.subscribe({
      next: () => { this.closeModal(); this.carregarUsuarios(); },
      error: (e) => { this.error = 'Erro ao salvar: ' + (e.error?.message || e.message); }
    });
  }

  handleDelete(id: number) {
    if (!window.confirm('Tem certeza que deseja deletar este usuário?')) return;
    this.usuarioService.deletarUsuario(id).subscribe({
      next: () => this.carregarUsuarios(),
      error: (e) => { this.error = 'Erro ao deletar: ' + (e.error?.message || e.message); }
    });
  }

  handleToggleStatus(id: number) {
    this.usuarioService.toggleStatusUsuario(id).subscribe({
      next: () => this.carregarUsuarios(),
      error: (e) => { this.error = 'Erro ao alterar status: ' + (e.error?.message || e.message); }
    });
  }

  rolesText(usuario: any): string {
    return usuario.roles?.length > 0 ? Array.from(usuario.roles as string[]).join(', ') : 'USER';
  }
}
