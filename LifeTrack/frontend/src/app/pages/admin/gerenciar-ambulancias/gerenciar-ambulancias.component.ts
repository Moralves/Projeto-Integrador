import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AmbulanciaService } from '../../../core/services/ambulancia.service';
import { BairroService } from '../../../core/services/bairro.service';
import { AnaliseEstrategicaService } from '../../../core/services/analise-estrategica.service';
import { AutocompleteSelectComponent } from '../../../shared/components/autocomplete-select/autocomplete-select.component';
import { renderChanges } from '../../../core/utils/render-changes';

@Component({
  selector: 'app-gerenciar-ambulancias',
  standalone: true,
  imports: [CommonModule, FormsModule, AutocompleteSelectComponent],
  templateUrl: './gerenciar-ambulancias.component.html',
  styleUrls: ['../admin.shared.css']
})
export class GerenciarAmbulanciasComponent implements OnInit {
  ambulancias: any[] = [];
  bairros: any[] = [];
  bairrosSugeridos: any[] = [];
  loading = true;
  loadingSugestoes = false;
  error = '';
  showModal = false;
  formData = { placa: '', tipo: 'BASICA', idBairroBase: '' as any };

  private ambulanciaService = inject(AmbulanciaService);
  private bairroService = inject(BairroService);
  private analiseService = inject(AnaliseEstrategicaService);
  private cdr = inject(ChangeDetectorRef);

  getOptionLabelBairro = (opt: any) => opt?.nome ?? opt;
  getOptionValueBairro = (opt: any) => opt?.id?.toString() ?? opt;

  ngOnInit() {
    forkJoin({
      ambulancias: this.ambulanciaService.listar(),
      bairros: this.bairroService.listar()
    }).pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: ({ ambulancias, bairros }) => {
        this.ambulancias = ambulancias;
        this.bairros = bairros;
        this.loading = false;
      },
      error: (e) => { this.error = 'Erro ao carregar dados: ' + (e.message || e); this.loading = false; }
    });
  }

  openModal() {
    this.formData = { placa: '', tipo: 'BASICA', idBairroBase: '' };
    this.bairrosSugeridos = [];
    this.showModal = true;
    this.carregarSugestoes('BASICA');
  }

  closeModal() { this.showModal = false; this.bairrosSugeridos = []; }

  onTipoChange(tipo: string) {
    this.formData.tipo = tipo;
    this.formData.idBairroBase = '';
    this.carregarSugestoes(tipo);
  }

  private carregarSugestoes(tipo: string) {
    this.loadingSugestoes = true;
    this.analiseService.obterBairrosSugeridos(tipo).pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe({
      next: (sugestoes) => { this.bairrosSugeridos = sugestoes.slice(0, 5); this.loadingSugestoes = false; },
      error: () => { this.bairrosSugeridos = []; this.loadingSugestoes = false; }
    });
  }

  selecionarSugestao(id: number) {
    this.formData.idBairroBase = id.toString();
  }

  handleSubmit() {
    this.error = '';
    if (!this.formData.idBairroBase) { this.error = 'Selecione um bairro base'; return; }
    const payload = { placa: this.formData.placa.trim(), tipo: this.formData.tipo, idBairroBase: parseInt(this.formData.idBairroBase) };
    this.ambulanciaService.cadastrar(payload).subscribe({
      next: () => { this.closeModal(); this.recarregarAmbulancias(); },
      error: (e) => { this.error = 'Erro ao salvar: ' + (e.error?.message || e.message); }
    });
  }

  toggleStatus(id: number, ativa: boolean) {
    const obs = ativa ? this.ambulanciaService.desativar(id) : this.ambulanciaService.ativar(id);
    obs.subscribe({
      next: () => this.recarregarAmbulancias(),
      error: (e) => { this.error = 'Erro ao alterar status: ' + (e.error?.message || e.message); }
    });
  }

  private recarregarAmbulancias() {
    this.ambulanciaService.listar().pipe(
      finalize(() => renderChanges(this.cdr))
    ).subscribe(d => this.ambulancias = d);
  }
}
