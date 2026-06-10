import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { OperatorLayoutComponent } from './layouts/operator-layout/operator-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { operadorGuard } from './core/guards/operador.guard';

// Admin pages
import { GerenciarUsuariosComponent } from './pages/admin/gerenciar-usuarios/gerenciar-usuarios.component';
import { GerenciarEquipesComponent } from './pages/admin/gerenciar-equipes/gerenciar-equipes.component';
import { GerenciarFuncionariosComponent } from './pages/admin/gerenciar-funcionarios/gerenciar-funcionarios.component';
import { GerenciarAmbulanciasComponent } from './pages/admin/gerenciar-ambulancias/gerenciar-ambulancias.component';
import { AnaliseEstrategicaComponent } from './pages/admin/analise-estrategica/analise-estrategica.component';
import { RelatoriosComponent } from './pages/admin/relatorios/relatorios.component';

// Operador pages
import { RegistrarOcorrenciaComponent } from './pages/operador/registrar-ocorrencia/registrar-ocorrencia.component';
import { ListarOcorrenciasComponent } from './pages/operador/listar-ocorrencias/listar-ocorrencias.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, adminGuard],
    children: [
      { path: 'usuarios', component: GerenciarUsuariosComponent },
      { path: 'equipes', component: GerenciarEquipesComponent },
      { path: 'funcionarios', component: GerenciarFuncionariosComponent },
      { path: 'ambulancias', component: GerenciarAmbulanciasComponent },
      { path: 'analise', component: AnaliseEstrategicaComponent },
      { path: 'relatorios', component: RelatoriosComponent },
      { path: '', redirectTo: 'analise', pathMatch: 'full' }
    ]
  },
  {
    path: 'operador',
    component: OperatorLayoutComponent,
    canActivate: [authGuard, operadorGuard],
    children: [
      { path: 'registrar', component: RegistrarOcorrenciaComponent },
      { path: 'ocorrencias', component: ListarOcorrenciasComponent },
      { path: '', redirectTo: 'ocorrencias', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
