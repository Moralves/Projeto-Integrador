import { useState, useEffect } from 'react';
import { historicoService } from '../services/historicoService';
import './HistoricoOcorrencia.css';

function HistoricoOcorrencia({ ocorrenciaId, atualizarEmTempoReal = true }) {
  const [historicos, setHistoricos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!ocorrenciaId) {
      setLoading(false);
      return;
    }

    const carregarHistorico = async () => {
      try {
        // Preservar posição do scroll durante atualizações
        const scrollPosition = window.scrollY || document.documentElement.scrollTop;
        
        const dados = await historicoService.buscarPorOcorrencia(ocorrenciaId);
        setHistoricos(dados);
        setError(null);
        
        // Restaurar posição do scroll após atualização
        requestAnimationFrame(() => {
          if (Math.abs((window.scrollY || document.documentElement.scrollTop) - scrollPosition) > 10) {
            window.scrollTo({
              top: scrollPosition,
              behavior: 'auto'
            });
          }
        });
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    carregarHistorico();

    // Atualizar em tempo real se habilitado
    let interval = null;
    if (atualizarEmTempoReal) {
      interval = setInterval(carregarHistorico, 3000); // Atualizar a cada 3 segundos
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [ocorrenciaId, atualizarEmTempoReal]);

  if (!ocorrenciaId) {
    return null;
  }

  if (loading) {
    return (
      <div className="historico-container">
        <div className="historico-loading">Carregando histórico...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="historico-container">
        <div className="historico-error">Erro ao carregar histórico: {error}</div>
      </div>
    );
  }

  const formatarData = (data) => {
    if (!data) return '-';
    const date = new Date(data);
    return date.toLocaleString('pt-BR');
  };

  const getAcaoIcon = (acao, acaoAmbulancia) => {
    // Se há ação da ambulância, usar ícone específico
    if (acaoAmbulancia) {
      if (acaoAmbulancia.includes('Indo até o local') || acaoAmbulancia.includes('Indo até')) {
        return '🚑';
      } else if (acaoAmbulancia.includes('Retornando') || acaoAmbulancia.includes('Retornou')) {
        return '🔄';
      }
    }
    
    const icons = {
      'ABERTURA': '🆕',
      'DESPACHO': '🚑',
      'CHEGADA': '📍',
      'CONCLUSAO': '✅',
      'ALTERACAO_STATUS': '🔄',
      'CANCELAMENTO': '❌'
    };
    return icons[acao] || '📝';
  };

  const getAcaoColor = (acao, acaoAmbulancia) => {
    // Se há ação da ambulância, usar cor específica
    if (acaoAmbulancia) {
      if (acaoAmbulancia.includes('Indo até o local') || acaoAmbulancia.includes('Indo até')) {
        return '#007bff'; // Azul para indo até o local
      } else if (acaoAmbulancia.includes('Retornando') || acaoAmbulancia.includes('Retornou')) {
        return '#ff9800'; // Laranja para retornando
      }
    }
    
    const colors = {
      'ABERTURA': '#17a2b8',
      'DESPACHO': '#007bff',
      'CHEGADA': '#28a745',
      'CONCLUSAO': '#6f42c1',
      'ALTERACAO_STATUS': '#ffc107',
      'CANCELAMENTO': '#dc3545'
    };
    return colors[acao] || '#6c757d';
  };

  const getStatusLabel = (statusAnterior, statusNovo) => {
    const statusLabels = {
      'ABERTA': 'Aberta',
      'DESPACHADA': 'Despachada',
      'EM_ATENDIMENTO': 'Em Atendimento',
      'CONCLUIDA': 'Concluída',
      'CANCELADA': 'Cancelada'
    };
    
    if (statusAnterior && statusNovo) {
      return `${statusLabels[statusAnterior] || statusAnterior} → ${statusLabels[statusNovo] || statusNovo}`;
    } else if (statusNovo) {
      return statusLabels[statusNovo] || statusNovo;
    }
    return null;
  };

  if (historicos.length === 0) {
    return (
      <div className="historico-container">
        <div className="historico-empty">Nenhum histórico registrado ainda.</div>
      </div>
    );
  }

  return (
    <div className="historico-container">
      <div className="historico-header">
        <h3>Histórico da Ocorrência</h3>
        {atualizarEmTempoReal && (
          <span className="historico-badge-live">🟢 Ao vivo</span>
        )}
      </div>
      <div className="historico-timeline">
        {historicos.map((historico, index) => {
          const acaoColor = getAcaoColor(historico.acao, historico.acaoAmbulancia);
          const acaoIcon = getAcaoIcon(historico.acao, historico.acaoAmbulancia);
          const statusLabel = getStatusLabel(historico.statusAnterior, historico.statusNovo);
          
          return (
            <div key={historico.id} className="historico-item">
              <div className="historico-item-icon" style={{ backgroundColor: acaoColor }}>
                {acaoIcon}
              </div>
              <div className="historico-item-content">
                <div className="historico-item-header">
                  <span className="historico-acao" style={{ color: acaoColor }}>
                    {historico.acao}
                  </span>
                  <span className="historico-data">{formatarData(historico.dataHora)}</span>
                </div>
                <div className="historico-item-body">
                  {historico.tipoOcorrencia && (
                    <div className="historico-info-row">
                      <strong>Tipo:</strong> {historico.tipoOcorrencia}
                    </div>
                  )}
                  {historico.placaAmbulancia && (
                    <div className="historico-info-row">
                      <strong>Ambulância:</strong> {historico.placaAmbulancia}
                      {historico.acaoAmbulancia && (
                        <span className="historico-acao-ambulancia" style={{ 
                          color: acaoColor,
                          fontWeight: '600',
                          marginLeft: '8px'
                        }}>
                          - {historico.acaoAmbulancia}
                        </span>
                      )}
                    </div>
                  )}
                  {statusLabel && (
                    <div className="historico-info-row" style={{ marginTop: '8px', marginBottom: '8px' }}>
                      <strong>Status da Ocorrência:</strong> 
                      <span style={{ 
                        marginLeft: '8px',
                        padding: '4px 8px',
                        backgroundColor: historico.statusNovo === 'CONCLUIDA' ? '#e0e7ff' : 
                                        historico.statusNovo === 'DESPACHADA' ? '#dbeafe' : '#f3f4f6',
                        color: historico.statusNovo === 'CONCLUIDA' ? '#3730a3' : 
                               historico.statusNovo === 'DESPACHADA' ? '#1e40af' : '#374151',
                        borderRadius: '6px',
                        fontSize: '0.875rem',
                        fontWeight: '600'
                      }}>
                        {statusLabel}
                      </span>
                    </div>
                  )}
                  {historico.descricaoAcao && (
                    <div className="historico-descricao">
                      {historico.descricaoAcao}
                    </div>
                  )}
                  {historico.statusAnterior && historico.statusNovo && (
                    <div className="historico-status-change">
                      <span className="status-badge status-anterior">{historico.statusAnterior}</span>
                      <span className="status-arrow">→</span>
                      <span className="status-badge status-novo">{historico.statusNovo}</span>
                    </div>
                  )}
                  {historico.usuarioNome && (
                    <div className="historico-usuario">
                      Por: {historico.usuarioNome} ({historico.usuarioPerfil})
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default HistoricoOcorrencia;

