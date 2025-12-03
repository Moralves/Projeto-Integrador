import { useState, useEffect } from 'react';
import { ocorrenciaService } from '../../../services/ocorrenciaService';
import { bairroService } from '../../../services/bairroService';
import AutocompleteSelect from '../../../components/AutocompleteSelect';
import '../OperatorLayout.css';

// Tipos de ocorrência pré-definidos
const TIPOS_OCORRENCIA = [
  'Acidente de Trânsito',
  'Atendimento Médico',
  'Resgate',
  'Incêndio',
  'Queda',
  'Intoxicação',
  'Parto',
  'Convulsão',
  'Parada Cardiorrespiratória',
  'Trauma',
  'Queimadura',
  'Afogamento',
  'Outros'
];

// Opções de gravidade
const GRAVIDADES = [
  { value: 'BAIXA', label: 'Baixa' },
  { value: 'MEDIA', label: 'Média' },
  { value: 'ALTA', label: 'Alta' }
];

function RegistrarOcorrencia({ onRegistroSuccess }) {
  const [bairros, setBairros] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const [formData, setFormData] = useState({
    idBairroLocal: '',
    tipoOcorrencia: '',
    gravidade: '',
    observacoes: ''
  });

  useEffect(() => {
    carregarBairros();
  }, []);

  const carregarBairros = async () => {
    try {
      const dados = await bairroService.listar();
      setBairros(dados);
    } catch (err) {
      setError('Erro ao carregar bairros: ' + err.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await ocorrenciaService.registrar({
        idBairroLocal: parseInt(formData.idBairroLocal),
        tipoOcorrencia: formData.tipoOcorrencia,
        gravidade: formData.gravidade,
        observacoes: formData.observacoes || null
      });

      setSuccess('Ocorrência registrada com sucesso!');
      
      // Limpar formulário
      const formDataLimpo = {
        idBairroLocal: '',
        tipoOcorrencia: '',
        gravidade: '',
        observacoes: ''
      };
      setFormData(formDataLimpo);

      if (onRegistroSuccess) {
        setTimeout(() => {
          onRegistroSuccess();
        }, 1500);
      }
    } catch (err) {
      setError('Erro ao registrar ocorrência: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '32px', color: '#1f2937', fontSize: '2rem' }}>
        Registrar Nova Ocorrência
      </h1>

      {error && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#fee2e2',
          color: '#dc2626',
          borderRadius: '8px',
          marginBottom: '24px',
          border: '1px solid #fecaca'
        }}>
          {error}
        </div>
      )}

      {success && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#d1fae5',
          color: '#065f46',
          borderRadius: '8px',
          marginBottom: '24px',
          border: '1px solid #a7f3d0'
        }}>
          {success}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '32px',
        borderRadius: '12px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
      }}>
        <div style={{ marginBottom: '24px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: '600',
            color: '#374151'
          }}>
            Bairro/Localização *
          </label>
          <AutocompleteSelect
            options={bairros}
            value={formData.idBairroLocal}
            onChange={(value) => setFormData({ ...formData, idBairroLocal: value })}
            placeholder="Digite ou selecione um bairro..."
            getOptionLabel={(opt) => opt.nome}
            getOptionValue={(opt) => opt.id.toString()}
            required={true}
          />
        </div>

        <div style={{ marginBottom: '24px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: '600',
            color: '#374151'
          }}>
            Tipo de Ocorrência *
          </label>
          <AutocompleteSelect
            options={TIPOS_OCORRENCIA}
            value={formData.tipoOcorrencia}
            onChange={(value) => setFormData({ ...formData, tipoOcorrencia: value })}
            placeholder="Digite ou selecione o tipo de ocorrência..."
            getOptionLabel={(opt) => opt}
            getOptionValue={(opt) => opt}
            required={true}
          />
        </div>

        <div style={{ marginBottom: '24px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: '600',
            color: '#374151'
          }}>
            Gravidade *
          </label>
          <AutocompleteSelect
            options={GRAVIDADES}
            value={formData.gravidade}
            onChange={(value) => setFormData({ ...formData, gravidade: value })}
            placeholder="Digite ou selecione a gravidade..."
            getOptionLabel={(opt) => opt.label}
            getOptionValue={(opt) => opt.value}
            required={true}
          />
        </div>

        <div style={{ marginBottom: '32px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: '600',
            color: '#374151'
          }}>
            Observações
          </label>
          <textarea
            value={formData.observacoes}
            onChange={(e) => setFormData({ ...formData, observacoes: e.target.value })}
            placeholder="Descreva detalhes adicionais da ocorrência..."
            rows={4}
            maxLength={1000}
            style={{
              width: '100%',
              padding: '12px',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              fontSize: '1rem',
              resize: 'vertical',
              fontFamily: 'inherit'
            }}
          />
          <div style={{
            fontSize: '0.875rem',
            color: '#6b7280',
            marginTop: '4px',
            textAlign: 'right'
          }}>
            {formData.observacoes.length}/1000
          </div>
        </div>

        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
          <button
            type="button"
            onClick={() => setFormData({
              idBairroLocal: '',
              tipoOcorrencia: '',
              gravidade: '',
              observacoes: ''
            })}
            disabled={loading}
            style={{
              padding: '12px 24px',
              backgroundColor: '#f3f4f6',
              color: '#374151',
              border: 'none',
              borderRadius: '8px',
              fontSize: '1rem',
              fontWeight: '600',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.6 : 1
            }}
          >
            Limpar
          </button>
          <button
            type="submit"
            disabled={loading}
            style={{
              padding: '12px 24px',
              backgroundColor: '#2563eb',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontSize: '1rem',
              fontWeight: '600',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.6 : 1
            }}
          >
            {loading ? 'Registrando...' : 'Registrar Ocorrência'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default RegistrarOcorrencia;

