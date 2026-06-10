import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  menuItems = [
    { path: '/admin/usuarios', label: 'Usuários', icon: '👥' },
    { path: '/admin/equipes', label: 'Equipes', icon: '👨‍👩‍👧‍👦' },
    { path: '/admin/funcionarios', label: 'Funcionários', icon: '👤' },
    { path: '/admin/ambulancias', label: 'Ambulâncias', icon: '🚑' },
    { path: '/admin/analise', label: 'Análise Estratégica', icon: '📊' },
    { path: '/admin/relatorios', label: 'Relatórios', icon: '📈' },
  ];

  handleLogout() {
    if (window.confirm('Deseja realmente sair?')) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }
}
