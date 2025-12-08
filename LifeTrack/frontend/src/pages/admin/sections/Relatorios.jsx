import React, { useState, useEffect } from 'react';
import { relatorioService } from '../../../services/relatorioService';
import HistoricoOcorrencia from '../../../components/HistoricoOcorrencia';
import SLATimer from '../../../components/SLATimer';
import '../AdminDashboard.css';

function Relatorios() {
  const [dados, setDados] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [ocorrenciasExpandidas, setOcorrenciasExpandidas] = useState(new Set());

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

  const toggleDetalhes = (ocorrenciaId) => {
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

  const deveMostrarDetalhes = (ocorrenciaId) => {
    return ocorrenciasExpandidas.has(ocorrenciaId);
  };

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Relatório de Ocorrências</h1>
        <button className="btn-primary" onClick={carregarRelatorio}>
          🔄 Atualizar
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Carregando relatório...</div>
      ) : (
        <div className="usuarios-table-container">
          <table className="usuarios-table">
            <thead>
              <tr>
                <th style={{ width: '50px' }}></th>
                <th>ID</th>
                <th>Data/Hora</th>
                <th>Bairro</th>
                <th>Tipo</th>
                <th>Gravidade</th>
                <th>Status</th>
                <th>Tempo Total</th>
                <th>Registrado por</th>
                <th>Despachado por</th>
                <th>Data Despacho</th>
                <th>Ambulância</th>
                <th>Distância (km)</th>
              </tr>
            </thead>
            <tbody>
              {dados.length === 0 ? (
                <tr>
                  <td colSpan="13" style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
                    Nenhuma ocorrência encontrada.
                  </td>
                </tr>
              ) : (
                dados.map(item => {
                  const expandido = deveMostrarDetalhes(item.id);
                  return (
                    <React.Fragment key={item.id}>
                      <tr style={{ cursor: 'pointer' }} onClick={() => toggleDetalhes(item.id)}>
                        <td style={{ textAlign: 'center' }}>
                          <span style={{ fontSize: '1.2rem' }}>
                            {expandido ? '▼' : '▶'}
                          </span>
                        </td>
                        <td>#{item.id}</td>
                        <td>{formatarData(item.dataHoraAbertura)}</td>
                        <td>{item.bairroNome || '-'}</td>
                        <td>{item.tipoOcorrencia}</td>
                        <td>{getGravidadeBadge(item.gravidade)}</td>
                        <td>{getStatusBadge(item.status)}</td>
                        <td>
                          <span style={{
                            fontWeight: '600',
                            color: item.status === 'CONCLUIDA' && item.tempoTotalFormatado ? '#059669' : '#374151'
                          }}>
                            {item.tempoTotalFormatado || '-'}
                          </span>
                        </td>
                        <td>
                          {item.usuarioRegistroNome ? (
                            <div>
                              <div style={{ fontWeight: '600' }}>{item.usuarioRegistroNome}</div>
                              <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>{item.usuarioRegistroLogin}</div>
                            </div>
                          ) : '-'}
                        </td>
                        <td>
                          {item.usuarioDespachoNome ? (
                            <div>
                              <div style={{ fontWeight: '600' }}>{item.usuarioDespachoNome}</div>
                              <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>{item.usuarioDespachoLogin}</div>
                            </div>
                          ) : '-'}
                        </td>
                        <td>{formatarData(item.dataHoraDespacho)}</td>
                        <td>{item.ambulanciaPlaca || '-'}</td>
                        <td>{item.distanciaKm ? item.distanciaKm.toFixed(2) : '-'}</td>
                      </tr>
                      {expandido && (
                        <>
                          {(item.status === 'ABERTA' || item.status === 'DESPACHADA' || item.status === 'EM_ATENDIMENTO' || item.status === 'CONCLUIDA') && (
                            <tr>
                              <td colSpan="13" style={{ padding: '0 16px 16px 16px', backgroundColor: '#f9fafb' }}>
                                <SLATimer ocorrenciaId={item.id} status={item.status} />
                              </td>
                            </tr>
                          )}
                          {(item.status === 'DESPACHADA' || item.status === 'EM_ATENDIMENTO' || item.status === 'CONCLUIDA') && (
                            <tr>
                              <td colSpan="13" style={{ padding: '0 16px 16px 16px', backgroundColor: '#f9fafb' }}>
                                <HistoricoOcorrencia 
                                  ocorrenciaId={item.id} 
                                  atualizarEmTempoReal={item.status !== 'CONCLUIDA'}
                                  statusOcorrencia={item.status}
                                />
                              </td>
                            </tr>
                          )}
                          <tr>
                            <td colSpan="13" style={{ padding: '16px', backgroundColor: '#f9fafb', borderTop: '1px solid #e5e7eb' }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px' }}>
                                {item.slaMinutos && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>SLA:</strong>
                                    <div style={{ fontSize: '1.125rem', fontWeight: '600' }}>{item.slaMinutos} minutos</div>
                                  </div>
                                )}
                                {item.tempoAtendimentoMinutos !== null && item.tempoAtendimentoMinutos !== undefined && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Tempo de Atendimento:</strong>
                                    <div style={{ fontSize: '1.125rem', fontWeight: '600' }}>{item.tempoAtendimentoMinutos} minutos</div>
                                  </div>
                                )}
                                {item.slaCumprido !== null && (
                                  <div>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>SLA Cumprido:</strong>
                                    <div style={{ 
                                      fontSize: '1.125rem', 
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
                                    <div style={{ fontSize: '1.125rem', fontWeight: '600', color: '#dc2626' }}>
                                      {item.tempoExcedidoMinutos} minutos
                                    </div>
                                  </div>
                                )}
                                {item.observacoes && (
                                  <div style={{ gridColumn: '1 / -1' }}>
                                    <strong style={{ color: '#6b7280', fontSize: '0.875rem' }}>Observações:</strong>
                                    <div style={{ marginTop: '4px', padding: '8px', backgroundColor: '#ffffff', borderRadius: '6px', fontSize: '0.875rem' }}>
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
                })
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default Relatorios;

