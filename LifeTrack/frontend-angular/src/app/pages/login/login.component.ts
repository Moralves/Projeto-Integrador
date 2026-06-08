import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  login: string = '';
  senha: string = '';
  error: string = '';
  loading: boolean = false;

  private authService = inject(AuthService);
  private router = inject(Router);

  handleSubmit() {
    this.error = '';
    this.loading = true;

    this.authService.login({ login: this.login, senha: this.senha }).subscribe({
      next: (res) => {
        const user = this.authService.getUsuarioLogado();
        if (user?.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/operador']);
        }
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'Erro ao fazer login. Verifique suas credenciais.';
        this.loading = false;
      }
    });
  }
}
