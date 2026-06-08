import { useState, useEffect } from 'react';
import { analiseEstrategicaService } from '../../../services/analiseEstrategicaService';
import '../AdminDashboard.css';

function AnaliseEstrategica() {
  const [bairrosSugeridos, setBairrosSugeridos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    carregarAnalise();
  }, []);

  const carregarAnalise = async () => {
    try {
      setLoading(true);
      const dados = await analiseEstrategicaService.obterBairrosSugeridos();
      setBairrosSugeridos(dados);
      setError('');
    } catch (err) {
      setError('Erro ao carregar análise estratégica: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const getPrioridadeBadge = (index) => {
    if (index === 0) {
      return { bg: '#fef3c7', color: '#92400e', text: '🥇 Alta Prioridade' };
    } else if (index === 1) {
      return { bg: '#e0e7ff', color: '#3730a3', text: '🥈 Média Prioridade' };
    } else if (index === 2) {
      return { bg: '#d1fae5', color: '#065f46', text: '🥉 Boa Opção' };
    }
    return { bg: '#f3f4f6', color: '#374151', text: 'Considerar' };
  };

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Análise Estratégica de Posicionamento</h1>
        <button className="btn-primary" onClick={carregarAnalise}>
          🔄 Atualizar
        </button>
      </div>

      <div style={{ 
        marginBottom: '20px', 
        padding: '16px', 
        backgroundColor: '#f0f7ff', 
        borderRadius: '8px',
        border: '1px solid #4a90e2'
      }}>
        <h3 style={{ margin: '0 0 8px 0', color: '#2c5aa0' }}>📊 Sobre esta análise</h3>
        <p style={{ margin: 0, color: '#4b5563', fontSize: '0.9rem' }}>
          Esta análise sugere os melhores bairros para posicionar novas ambulâncias, 
          considerando o número de ocorrências, tempo médio de resposta calculado com Dijkstra, 
          e a quantidade de ambulâncias já existentes em cada bairro.
        </p>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Carregando análise estratégica...</div>
      ) : (
        <div className="usuarios-table-container">
          <table className="usuarios-table">
            <thead>
              <tr>
                <th>Prioridade</th>
                <th>Bairro</th>
                <th>Justificativa</th>
                <th>Conexões Diretas</th>
                <th>Ocorrências</th>
                <th>Tempo Médio Resposta</th>
                <th>Ambulâncias Existentes</th>
              </tr>
            </thead>
            <tbody>
              {bairrosSugeridos.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
                    Nenhum bairro encontrado para análise.
                  </td>
                </tr>
              ) : (
                bairrosSugeridos.map((bairro, index) => {
                  const badge = getPrioridadeBadge(index);
                  return (
                    <tr key={bairro.id}>
                      <td>
                        <span style={{
                          padding: '6px 12px',
                          borderRadius: '12px',
                          fontSize: '0.875rem',
                          fontWeight: '600',
                          backgroundColor: badge.bg,
                          color: badge.color
                        }}>
                          {badge.text}
                        </span>
                      </td>
                      <td style={{ fontWeight: '600', fontSize: '1rem' }}>
                        {bairro.nome}
                      </td>
                      <td style={{ color: '#4b5563', fontSize: '0.9rem' }}>
                        {bairro.justificativa || 'Bairro estratégico para expansão da cobertura'}
                      </td>
                      <td>
                        {bairro.bairrosAlcancaveis !== undefined && bairro.bairrosAlcancaveis > 0 ? (
                          <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '4px',
                            padding: '4px 8px',
                            backgroundColor: bairro.bairrosAlcancaveis >= 5 ? '#d1fae5' : bairro.bairrosAlcancaveis >= 3 ? '#fef3c7' : '#fee2e2',
                            borderRadius: '6px',
                            fontSize: '0.875rem',
                            fontWeight: '600',
                            color: bairro.bairrosAlcancaveis >= 5 ? '#065f46' : bairro.bairrosAlcancaveis >= 3 ? '#92400e' : '#991b1b'
                          }}>
                            🔗 {bairro.bairrosAlcancaveis} conexão(ões)
                          </span>
                        ) : (
                          <span style={{ color: '#9ca3af' }}>0 conexões</span>
                        )}
                      </td>
                      <td>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                          padding: '4px 8px',
                          backgroundColor: bairro.ocorrenciasNoBairro > 0 ? '#fef3c7' : '#f3f4f6',
                          borderRadius: '6px',
                          fontSize: '0.875rem',
                          fontWeight: '600'
                        }}>
                          📊 {bairro.ocorrenciasNoBairro}
                        </span>
                      </td>
                      <td>
                        {bairro.tempoMedioResposta > 0 ? (
                          <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '4px',
                            padding: '4px 8px',
                            backgroundColor: bairro.tempoMedioResposta < 15 ? '#d1fae5' : '#fee2e2',
                            borderRadius: '6px',
                            fontSize: '0.875rem',
                            fontWeight: '600',
                            color: bairro.tempoMedioResposta < 15 ? '#065f46' : '#991b1b'
                          }}>
                            ⏱️ {bairro.tempoMedioResposta.toFixed(1)} min
                          </span>
                        ) : (
                          <span style={{ color: '#9ca3af' }}>-</span>
                        )}
                      </td>
                      <td>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                          padding: '4px 8px',
                          backgroundColor: bairro.ambulanciasExistentes === 0 ? '#fee2e2' : '#f3f4f6',
                          borderRadius: '6px',
                          fontSize: '0.875rem',
                          fontWeight: '600'
                        }}>
                          🚑 {bairro.ambulanciasExistentes}
                        </span>
                      </td>
                    </tr>
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

export default AnaliseEstrategica;

