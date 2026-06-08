import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-operator-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './operator-layout.component.html',
  styleUrls: ['./operator-layout.component.css']
})
export class OperatorLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  menuItems = [
    { path: '/operador/registrar', label: 'Registrar Ocorrência', icon: '📝' },
    { path: '/operador/ocorrencias', label: 'Ocorrências', icon: '📋' },
  ];

  handleLogout() {
    if (window.confirm('Deseja realmente sair?')) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }
}
