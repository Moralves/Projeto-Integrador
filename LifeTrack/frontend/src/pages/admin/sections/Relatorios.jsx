import { useState, useEffect } from 'react';
import { relatorioService } from '../../../services/relatorioService';
import '../AdminDashboard.css';

function Relatorios() {
  const [dados, setDados] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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
                <th>ID</th>
                <th>Data/Hora</th>
                <th>Bairro</th>
                <th>Tipo</th>
                <th>Gravidade</th>
                <th>Status</th>
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
                  <td colSpan="11" style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
                    Nenhuma ocorrência encontrada.
                  </td>
                </tr>
              ) : (
                dados.map(item => (
                  <tr key={item.id}>
                    <td>#{item.id}</td>
                    <td>{formatarData(item.dataHoraAbertura)}</td>
                    <td>{item.bairroNome || '-'}</td>
                    <td>{item.tipoOcorrencia}</td>
                    <td>{getGravidadeBadge(item.gravidade)}</td>
                    <td>{getStatusBadge(item.status)}</td>
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
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default Relatorios;

