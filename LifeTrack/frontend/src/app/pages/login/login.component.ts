import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { renderChanges } from '../../core/utils/render-changes';

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
  private cdr = inject(ChangeDetectorRef);

  handleSubmit() {
    this.error = '';
    this.loading = true;

    this.authService.login({ login: this.login, senha: this.senha }).pipe(
      finalize(() => {
        this.loading = false;
        renderChanges(this.cdr);
      })
    ).subscribe({
      next: (res) => {
        const user = this.authService.getUsuarioLogado() ?? res.user;
        if (user?.role === 'ADMIN' || user?.perfil === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/operador']);
        }
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'Erro ao fazer login. Verifique suas credenciais.';
      }
    });
  }
}
