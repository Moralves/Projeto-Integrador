import { useState, useEffect } from 'react';
import { ocorrenciaService } from '../../../services/ocorrenciaService';
import '../OperatorLayout.css';

function ListarOcorrencias() {
  const [ocorrencias, setOcorrencias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filtroStatus, setFiltroStatus] = useState('TODAS');
  const [filtroGravidade, setFiltroGravidade] = useState('TODAS');
  const [busca, setBusca] = useState('');
  const [despachando, setDespachando] = useState(null);

  useEffect(() => {
    carregarOcorrencias();
  }, []);

  const carregarOcorrencias = async () => {
    try {
      setLoading(true);
      const dados = await ocorrenciaService.listar();
      setOcorrencias(dados);
      setError('');
    } catch (err) {
      setError('Erro ao carregar ocorrências: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDespachar = async (id) => {
    if (!window.confirm('Deseja realmente despachar esta ocorrência?')) {
      return;
    }

    try {
      setDespachando(id);
      setError('');
      await ocorrenciaService.despachar(id);
      await carregarOcorrencias();
      alert('Ocorrência despachada com sucesso!');
    } catch (err) {
      setError('Erro ao despachar ocorrência: ' + err.message);
      alert('Erro ao despachar: ' + err.message);
    } finally {
      setDespachando(null);
    }
  };

  const formatarData = (data) => {
    if (!data) return '-';
    const date = new Date(data);
    return date.toLocaleString('pt-BR');
  };

  const getStatusBadge = (status) => {
    const badges = {
      'ABERTA': { bg: '#d1fae5', color: '#065f46', text: '🟢 ABERTA' },
      'DESPACHADA': { bg: '#dbeafe', color: '#1e40af', text: '🔵 DESPACHADA' },
      'EM_ATENDIMENTO': { bg: '#fef3c7', color: '#92400e', text: '🟡 EM ATENDIMENTO' },
      'CONCLUIDA': { bg: '#e0e7ff', color: '#3730a3', text: '✅ CONCLUÍDA' },
      'CANCELADA': { bg: '#fee2e2', color: '#991b1b', text: '🔴 CANCELADA' }
    };
    const badge = badges[status] || { bg: '#f3f4f6', color: '#374151', text: status };
    return (
      <span style={{
        padding: '4px 12px',
        borderRadius: '12px',
        fontSize: '0.875rem',
        fontWeight: '600',
        backgroundColor: badge.bg,
        color: badge.color
      }}>
        {badge.text}
      </span>
    );
  };

  const getGravidadeBadge = (gravidade) => {
    const badges = {
      'BAIXA': { bg: '#d1fae5', color: '#065f46' },
      'MEDIA': { bg: '#fef3c7', color: '#92400e' },
      'ALTA': { bg: '#fee2e2', color: '#dc2626' }
    };
    const badge = badges[gravidade] || { bg: '#f3f4f6', color: '#374151' };
    return (
      <span style={{
        padding: '4px 12px',
        borderRadius: '12px',
        fontSize: '0.875rem',
        fontWeight: '600',
        backgroundColor: badge.bg,
        color: badge.color
      }}>
        {gravidade}
      </span>
    );
  };

  const ocorrenciasFiltradas = ocorrencias.filter(oc => {
    const matchStatus = filtroStatus === 'TODAS' || oc.status === filtroStatus;
    const matchGravidade = filtroGravidade === 'TODAS' || oc.gravidade === filtroGravidade;
    const matchBusca = !busca || 
      (oc.tipoOcorrencia && oc.tipoOcorrencia.toLowerCase().includes(busca.toLowerCase())) ||
      (oc.bairroLocal && oc.bairroLocal.nome && oc.bairroLocal.nome.toLowerCase().includes(busca.toLowerCase()));
    return matchStatus && matchGravidade && matchBusca;
  });

  return (
    <div>
      <div style={{ marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ color: '#1f2937', fontSize: '2rem', margin: 0 }}>
          Ocorrências Registradas
        </h1>
        <button
          onClick={carregarOcorrencias}
          style={{
            padding: '10px 20px',
            backgroundColor: '#2563eb',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            fontSize: '0.95rem',
            fontWeight: '600',
            cursor: 'pointer'
          }}
        >
          🔄 Atualizar
        </button>
      </div>

      {error && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#fee2e2',
          color: '#dc2626',
          borderRadius: '8px',
          marginBottom: '24px'
        }}>
          {error}
        </div>
      )}

      <div style={{
        backgroundColor: 'white',
        padding: '24px',
        borderRadius: '12px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        marginBottom: '24px'
      }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 2fr', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#374151' }}>
              Status
            </label>
            <select
              value={filtroStatus}
              onChange={(e) => setFiltroStatus(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px'
              }}
            >
              <option value="TODAS">Todas</option>
              <option value="ABERTA">Abertas</option>
              <option value="DESPACHADA">Despachadas</option>
              <option value="EM_ATENDIMENTO">Em Atendimento</option>
              <option value="CONCLUIDA">Concluídas</option>
              <option value="CANCELADA">Canceladas</option>
            </select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#374151' }}>
              Gravidade
            </label>
            <select
              value={filtroGravidade}
              onChange={(e) => setFiltroGravidade(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px'
              }}
            >
              <option value="TODAS">Todas</option>
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Média</option>
              <option value="ALTA">Alta</option>
            </select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#374151' }}>
              Buscar
            </label>
            <input
              type="text"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              placeholder="Buscar por tipo ou bairro..."
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px'
              }}
            />
          </div>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
          Carregando ocorrências...
        </div>
      ) : ocorrenciasFiltradas.length === 0 ? (
        <div style={{
          backgroundColor: 'white',
          padding: '40px',
          borderRadius: '12px',
          textAlign: 'center',
          color: '#6b7280'
        }}>
          Nenhuma ocorrência encontrada.
        </div>
      ) : (
        <div style={{
          backgroundColor: 'white',
          borderRadius: '12px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          overflow: 'hidden'
        }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>ID</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Data/Hora</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Bairro</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Tipo</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Gravidade</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Status</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Ações</th>
                </tr>
              </thead>
              <tbody>
                {ocorrenciasFiltradas.map(oc => (
                  <tr key={oc.id} style={{ borderBottom: '1px solid #e5e7eb' }}>
                    <td style={{ padding: '16px', color: '#6b7280' }}>#{oc.id}</td>
                    <td style={{ padding: '16px', color: '#374151' }}>{formatarData(oc.dataHoraAbertura)}</td>
                    <td style={{ padding: '16px', color: '#374151' }}>
                      {oc.bairroLocal ? oc.bairroLocal.nome : '-'}
                    </td>
                    <td style={{ padding: '16px', color: '#374151' }}>{oc.tipoOcorrencia}</td>
                    <td style={{ padding: '16px' }}>{getGravidadeBadge(oc.gravidade)}</td>
                    <td style={{ padding: '16px' }}>{getStatusBadge(oc.status)}</td>
                    <td style={{ padding: '16px' }}>
                      {oc.status === 'ABERTA' && (
                        <button
                          onClick={() => handleDespachar(oc.id)}
                          disabled={despachando === oc.id}
                          style={{
                            padding: '8px 16px',
                            backgroundColor: '#10b981',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            fontSize: '0.875rem',
                            fontWeight: '600',
                            cursor: despachando === oc.id ? 'not-allowed' : 'pointer',
                            opacity: despachando === oc.id ? 0.6 : 1
                          }}
                        >
                          {despachando === oc.id ? 'Despachando...' : '🚑 Despachar'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default ListarOcorrencias;

