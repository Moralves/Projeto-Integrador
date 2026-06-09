import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { OcorrenciaService } from '../../../core/services/ocorrencia.service';
import { BairroService } from '../../../core/services/bairro.service';
import { AutocompleteSelectComponent } from '../../../shared/components/autocomplete-select/autocomplete-select.component';

const TIPOS_OCORRENCIA = [
  'Acidente de Trânsito', 'Atendimento Médico', 'Resgate', 'Incêndio',
  'Queda', 'Intoxicação', 'Parto', 'Convulsão',
  'Parada Cardiorrespiratória', 'Trauma', 'Queimadura', 'Afogamento', 'Outros'
];

const GRAVIDADES = [
  { value: 'BAIXA', label: 'Baixa' },
  { value: 'MEDIA', label: 'Média' },
  { value: 'ALTA',  label: 'Alta'  }
];

@Component({
  selector: 'app-registrar-ocorrencia',
  standalone: true,
  imports: [CommonModule, FormsModule, AutocompleteSelectComponent],
  templateUrl: './registrar-ocorrencia.component.html',
  styleUrls: ['./registrar-ocorrencia.component.css']
})
export class RegistrarOcorrenciaComponent implements OnInit {
  bairros: any[] = [];
  loading = false;
  error = '';
  success = '';

  readonly tiposOcorrencia = TIPOS_OCORRENCIA;
  readonly gravidades = GRAVIDADES;

  formData = {
    idBairroLocal: '' as any,
    tipoOcorrencia: '' as any,
    gravidade: '' as any,
    observacoes: ''
  };

  private ocorrenciaService = inject(OcorrenciaService);
  private bairroService = inject(BairroService);
  private router = inject(Router);

  getOptionLabelBairro = (opt: any) => opt?.nome ?? opt;
  getOptionValueBairro = (opt: any) => opt?.id?.toString() ?? opt;

  getOptionLabelTipo = (opt: any) => opt;
  getOptionValueTipo = (opt: any) => opt;

  getOptionLabelGravidade = (opt: any) => opt?.label ?? opt;
  getOptionValueGravidade = (opt: any) => opt?.value ?? opt;

  ngOnInit() {
    this.carregarBairros();
  }

  private carregarBairros() {
    this.bairroService.listar().subscribe({
      next: (dados) => (this.bairros = dados),
      error: (err) => (this.error = 'Erro ao carregar bairros: ' + (err.message || err))
    });
  }

  limpar() {
    this.formData = { idBairroLocal: '', tipoOcorrencia: '', gravidade: '', observacoes: '' };
    this.error = '';
    this.success = '';
  }

  handleSubmit() {
    this.error = '';
    this.success = '';

    if (!this.formData.idBairroLocal) { this.error = 'Selecione um bairro'; return; }
    if (!this.formData.tipoOcorrencia?.trim()) { this.error = 'Informe o tipo de ocorrência'; return; }
    if (!this.formData.gravidade) { this.error = 'Selecione a gravidade'; return; }

    this.loading = true;

    const payload = {
      idBairroLocal: parseInt(this.formData.idBairroLocal),
      tipoOcorrencia: this.formData.tipoOcorrencia.trim(),
      gravidade: String(this.formData.gravidade).toUpperCase(),
      observacoes: this.formData.observacoes?.trim() || null
    };

    this.ocorrenciaService.registrar(payload).subscribe({
      next: () => {
        this.success = 'Ocorrência registrada com sucesso!';
        this.limpar();
        setTimeout(() => this.router.navigate(['/operador/ocorrencias']), 1500);
      },
      error: (err) => {
        this.error = 'Erro ao registrar ocorrência: ' + (err.error?.message || err.message || err);
        this.loading = false;
      },
      complete: () => (this.loading = false)
    });
  }
}
