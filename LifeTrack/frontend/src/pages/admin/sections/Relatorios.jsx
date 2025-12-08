import React, { useState, useEffect } from 'react';
import { relatorioService } from '../../../services/relatorioService';
import HistoricoOcorrencia from '../../../components/HistoricoOcorrencia';
import SLATimer from '../../../components/SLATimer';

function Relatorios() {
  const [dados, setDados] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [ocorrenciasExpandidas, setOcorrenciasExpandidas] = useState(new Set());
  const [filtroStatus, setFiltroStatus] = useState('TODAS');
  const [filtroGravidade, setFiltroGravidade] = useState('TODAS');
  const [busca, setBusca] = useState('');

  useEffect(() => {
    carregarRelatorio();
  }, []);

  const carregarRelatorio = async () => {
    try {
      setLoading(true);
      const dadosRelatorio = await relatorioService.relatorioOcorrencias();
      setDados(dadosRelatorio);
      setError('');
    } catch (err) {
      setError('Erro ao carregar relatório: ' + err.message);
    } finally {
      setLoading(false);
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

  const ocorrenciasFiltradas = dados.filter(item => {
    const matchStatus = filtroStatus === 'TODAS' || item.status === filtroStatus;
    const matchGravidade = filtroGravidade === 'TODAS' || item.gravidade === filtroGravidade;
    const matchBusca = !busca || 
      (item.tipoOcorrencia && item.tipoOcorrencia.toLowerCase().includes(busca.toLowerCase())) ||
      (item.bairroNome && item.bairroNome.toLowerCase().includes(busca.toLowerCase()));
    return matchStatus && matchGravidade && matchBusca;
  });

  const toggleExpandirOcorrencia = (ocorrenciaId) => {
    setOcorrenciasExpandidas(prev => {
      const novo = new Set(prev);
      if (novo.has(ocorrenciaId)) {
        novo.delete(ocorrenciaId);
      } else {
        novo.add(ocorrenciaId);
      }
      return novo;
    });
  };

  const isOcorrenciaExpandida = (ocorrenciaId) => {
    return ocorrenciasExpandidas.has(ocorrenciaId);
  };

  return (
    <div>
      <div style={{ marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ color: '#1f2937', fontSize: '2rem', margin: 0 }}>
          Relatório de Ocorrências
        </h1>
        <button
          onClick={carregarRelatorio}
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
          Carregando relatório...
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
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Tempo Total</th>
                  <th style={{ padding: '16px', textAlign: 'left', fontWeight: '600', color: '#374151' }}>Ações</th>
                </tr>
              </thead>
              <tbody>
                {ocorrenciasFiltradas.map(item => {
                  const isConcluida = item.status === 'CONCLUIDA';
                  const isExpandida = isOcorrenciaExpandida(item.id);
                  const deveMostrarDetalhes = !isConcluida || isExpandida;

                  return (
                    <React.Fragment key={item.id}>
                      <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
                        <td style={{ padding: '16px', color: '#6b7280' }}>#{item.id}</td>
                        <td style={{ padding: '16px', color: '#374151' }}>{formatarData(item.dataHoraAbertura)}</td>
                        <td style={{ padding: '16px', color: '#374151' }}>
                          {item.bairroNome || '-'}
                        </td>
                        <td style={{ padding: '16px', color: '#374151' }}>{item.tipoOcorrencia}</td>
                        <td style={{ padding: '16px' }}>{getGravidadeBadge(item.gravidade)}</td>
                        <td style={{ padding: '16px' }}>{getStatusBadge(item.status)}</td>
                        <td style={{ padding: '16px' }}>
                          <span style={{
                            fontWeight: '600',
                            color: item.status === 'CONCLUIDA' && item.tempoTotalFormatado ? '#059669' : '#374151'
                          }}>
                            {item.tempoTotalFormatado || '-'}
                          </span>
                        </td>
                        <td style={{ padding: '16px' }}>
                          <button
                            onClick={() => toggleExpandirOcorrencia(item.id)}
                            style={{
                              padding: '8px 16px',
                              backgroundColor: '#6366f1',
                              color: 'white',
                              border: 'none',
                              borderRadius: '6px',
                              fontSize: '0.875rem',
                              fontWeight: '600',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '6px'
                            }}
                          >
                            {isExpandida ? '▼ Ocultar Detalhes' : '▶ Ver Detalhes'}
                          </button>
                        </td>
                      </tr>
                      {deveMostrarDetalhes && (item.status === 'ABERTA' || item.status === 'DESPACHADA' || item.status === 'EM_ATENDIMENTO' || item.status === 'CONCLUIDA') && (
                        <>
                          <tr>
                            <td colSpan="8" style={{ padding: '0 16px 16px 16px' }}>
                              <SLATimer ocorrenciaId={item.id} status={item.status} />
                            </td>
                          </tr>
                          {(item.status === 'DESPACHADA' || item.status === 'EM_ATENDIMENTO' || item.status === 'CONCLUIDA') && (
                            <tr>
                              <td colSpan="8" style={{ padding: '0 16px 16px 16px' }}>
                                <HistoricoOcorrencia 
                                  ocorrenciaId={item.id} 
                                  atualizarEmTempoReal={item.status !== 'CONCLUIDA'}
                                  statusOcorrencia={item.status}
                                />
                              </td>
                            </tr>
                          )}
                          <tr>
                            <td colSpan="8" style={{ padding: '16px', backgroundColor: '#f9fafb', borderTop: '1px solid #e5e7eb' }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px' }}>
                                {item.usuarioRegistroNome && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Registrado por:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{item.usuarioRegistroNome}</div>
                                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>{item.usuarioRegistroLogin}</div>
                                  </div>
                                )}
                                {item.usuarioDespachoNome && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Despachado por:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{item.usuarioDespachoNome}</div>
                                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>{item.usuarioDespachoLogin}</div>
                                  </div>
                                )}
                                {item.dataHoraDespacho && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Data Despacho:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{formatarData(item.dataHoraDespacho)}</div>
                                  </div>
                                )}
                                {item.ambulanciaPlaca && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Ambulância:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{item.ambulanciaPlaca}</div>
                                  </div>
                                )}
                                {item.distanciaKm !== null && item.distanciaKm !== undefined && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Distância:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{item.distanciaKm.toFixed(2)} km</div>
                                  </div>
                                )}
                                {item.slaMinutos && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>SLA:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{item.slaMinutos} minutos</div>
                                  </div>
                                )}
                                {item.tempoAtendimentoMinutos !== null && item.tempoAtendimentoMinutos !== undefined && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Tempo de Atendimento:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600' }}>{item.tempoAtendimentoMinutos} minutos</div>
                                  </div>
                                )}
                                {item.slaCumprido !== null && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>SLA Cumprido:</strong>
                                    <div style={{ 
                                      fontSize: '1rem', 
                                      fontWeight: '600',
                                      color: item.slaCumprido ? '#059669' : '#dc2626'
                                    }}>
                                      {item.slaCumprido ? '✅ Sim' : '❌ Não'}
                                    </div>
                                  </div>
                                )}
                                {item.tempoExcedidoMinutos !== null && item.tempoExcedidoMinutos !== undefined && item.tempoExcedidoMinutos > 0 && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Tempo Excedido:</strong>
                                    <div style={{ fontSize: '1rem', fontWeight: '600', color: '#dc2626' }}>
                                      {item.tempoExcedidoMinutos} minutos
                                    </div>
                                  </div>
                                )}
                                {item.observacoes && (
                                  <div style={{ gridColumn: '1 / -1' }}>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Observações:</strong>
                                    <div style={{ marginTop: '4px', padding: '12px', backgroundColor: '#ffffff', borderRadius: '6px', fontSize: '0.875rem', border: '1px solid #e5e7eb' }}>
                                      {item.observacoes}
                                    </div>
                                  </div>
                                )}
                              </div>
                            </td>
                          </tr>
                        </>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default Relatorios;
