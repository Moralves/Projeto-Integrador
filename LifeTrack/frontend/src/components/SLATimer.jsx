import { useState, useEffect } from 'react';
import { ocorrenciaService } from '../services/ocorrenciaService';
import './SLATimer.css';

function SLATimer({ ocorrenciaId, status }) {
  const [timer, setTimer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Status que devem mostrar o timer
  const statusComTimer = ['ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA'];

  useEffect(() => {
    if (!ocorrenciaId || !statusComTimer.includes(status)) {
      setLoading(false);
      return;
    }

    let interval = null;
    let isMounted = true;
    let retornouBase = false; // Flag para controlar se já retornou

    const carregarTimer = async () => {
      // Se já retornou, não atualizar mais
      if (retornouBase) {
        return;
      }

      try {
        const dados = await ocorrenciaService.obterTimer(ocorrenciaId);
        if (!isMounted) return;
        
        // Se retornou à base, marcar flag e parar o intervalo IMEDIATAMENTE
        if (dados && dados.retornouBase === true) {
          retornouBase = true;
          if (interval) {
            clearInterval(interval);
            interval = null;
          }
          setTimer(dados); // Manter os dados finais
          setError(null);
          setLoading(false);
          return; // Não continuar atualizando
        }
        
        setTimer(dados);
        setError(null);
      } catch (err) {
        if (!isMounted) return;
        setError(err.message);
      } finally {
        if (isMounted && !retornouBase) {
          setLoading(false);
        }
      }
    };

    // Carregar inicialmente e verificar se já retornou
    ocorrenciaService.obterTimer(ocorrenciaId).then(dados => {
      if (!isMounted) return;
      
      // Verificar se já retornou no carregamento inicial
      if (dados && dados.retornouBase === true) {
        retornouBase = true;
        setTimer(dados);
        setError(null);
        setLoading(false);
        // Não iniciar intervalo se já retornou
        return;
      }
      
      setTimer(dados);
      setError(null);
      setLoading(false);
      
      // Continuar atualizando se ainda não retornou (mesmo se status for CONCLUIDA, para atualizar o tempo de retorno)
      if (dados && dados.retornouBase !== true) {
        interval = setInterval(carregarTimer, 2000);
      }
    }).catch(err => {
      if (!isMounted) return;
      setError(err.message);
      setLoading(false);
    });

    return () => {
      isMounted = false;
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [ocorrenciaId, status]);

  if (!ocorrenciaId || !statusComTimer.includes(status)) {
    return null;
  }

  if (loading) {
    return (
      <div className="sla-timer-container">
        <div className="sla-loading">Carregando timer...</div>
      </div>
    );
  }

  if (error || !timer) {
    return null;
  }

  const getSlaStatusClass = () => {
    if (timer.slaExcedido) return 'sla-excedido';
    if (timer.slaEmRisco) return 'sla-risco';
    return 'sla-ok';
  };

  const getSlaStatusText = () => {
    if (timer.slaExcedido) return 'SLA Excedido';
    if (timer.slaEmRisco) return 'SLA em Risco';
    return 'SLA OK';
  };

  const calcularPercentualSla = () => {
    if (!timer.slaMinutos || timer.slaMinutos === 0) return 0;
    const percentual = ((timer.tempoSlaDecorridoMinutos || 0) / timer.slaMinutos) * 100;
    return Math.min(100, Math.max(0, percentual));
  };

  const calcularEtapasProgresso = () => {
    if (!timer.slaMinutos || timer.slaMinutos === 0) {
      return { ateDespacho: 0, ateChegada: 0, retorno: 0, total: 0 };
    }

    const slaTotalMinutos = timer.slaMinutos;
    let ateDespachoPercentual = 0;
    let ateChegadaPercentual = 0;
    let retornoPercentual = 0;

    // Calcular tempo até despacho (desde abertura até despacho)
    if (timer.foiDespachada && timer.dataHoraDespacho && timer.dataHoraAbertura) {
      try {
        const tempoAteDespachoSegundos = (new Date(timer.dataHoraDespacho) - new Date(timer.dataHoraAbertura)) / 1000;
        const tempoAteDespachoMinutos = tempoAteDespachoSegundos / 60;
        ateDespachoPercentual = (tempoAteDespachoMinutos / slaTotalMinutos) * 100;
      } catch (e) {
        ateDespachoPercentual = 0;
      }
    }

    // Calcular tempo de ida (despacho até chegada)
    if (timer.tempoAteChegadaMinutos !== null && timer.tempoAteChegadaMinutos !== undefined) {
      ateChegadaPercentual = (timer.tempoAteChegadaMinutos / slaTotalMinutos) * 100;
    }

    // Calcular tempo de retorno (apenas se OS está finalizada)
    if (timer.foiConcluida && timer.tempoRetornoDecorridoMinutos !== null && timer.tempoRetornoDecorridoMinutos !== undefined) {
      retornoPercentual = (timer.tempoRetornoDecorridoMinutos / slaTotalMinutos) * 100;
    }

    // Calcular o total usado do SLA (apenas tempo até chegada, sem retorno)
    const tempoSlaUsado = (timer.tempoSlaDecorridoMinutos || 0);
    const totalPercentual = (tempoSlaUsado / slaTotalMinutos) * 100;

    // Normalizar para que a soma das etapas visíveis não exceda o total
    const tempoAteChegadaTotal = ateDespachoPercentual + ateChegadaPercentual;

    // Se o tempo usado é diferente do esperado, ajustar proporcionalmente
    if (tempoAteChegadaTotal > 0 && totalPercentual > 0) {
      const fatorAjuste = Math.min(1, totalPercentual / tempoAteChegadaTotal);
      ateDespachoPercentual *= fatorAjuste;
      ateChegadaPercentual *= fatorAjuste;
    }

    return {
      ateDespacho: Math.min(100, Math.max(0, ateDespachoPercentual)),
      ateChegada: Math.min(100, Math.max(0, ateChegadaPercentual)),
      retorno: Math.min(100, Math.max(0, retornoPercentual)),
      total: Math.min(100, Math.max(0, totalPercentual))
    };
  };

  const percentual = calcularPercentualSla();
  const etapas = calcularEtapasProgresso();

  return (
    <div className={`sla-timer-container ${getSlaStatusClass()}`}>
      <div className="sla-header">
        <div className="sla-status-badge">
          <span className="sla-status-icon">
            {timer.slaExcedido ? '⚠️' : timer.slaEmRisco ? '⏱️' : '✅'}
          </span>
          <span className="sla-status-text">{getSlaStatusText()}</span>
        </div>
        <div className="sla-tempo-restante">
          {timer.tempoRestanteFormatado || '--'}
        </div>
      </div>
      
      <div className="sla-progress-bar-container">
        <div className="sla-progress-segmented">
          {/* Etapa 1: Tempo até despacho */}
          {etapas.ateDespacho > 0 && (
            <div 
              className="sla-progress-segment sla-segment-ate-despacho"
              style={{ width: `${Math.max(1, etapas.ateDespacho)}%` }}
              title="Tempo desde abertura até despacho"
            />
          )}
          {/* Etapa 2: Tempo de deslocamento (despacho até chegada) */}
          {etapas.ateChegada > 0 && (
            <div 
              className="sla-progress-segment sla-segment-ida"
              style={{ width: `${Math.max(1, etapas.ateChegada)}%` }}
              title="Tempo de deslocamento: base → destino"
            />
          )}
          {/* Etapa 3: Tempo de retorno (apenas se OS está finalizada) */}
          {timer.foiConcluida && etapas.retorno > 0 && (
            <div 
              className="sla-progress-segment sla-segment-retorno"
              style={{ width: `${Math.max(1, etapas.retorno)}%` }}
              title="Tempo de retorno à base (não conta para SLA)"
            />
          )}
          {/* Barra de fundo mostrando progresso total do SLA */}
          {percentual > 0 && (
            <div 
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: `${Math.min(100, percentual)}%`,
                height: '100%',
                backgroundColor: timer.slaExcedido ? '#dc3545' : timer.slaEmRisco ? '#ffc107' : '#10b981',
                opacity: 0.15,
                zIndex: 0,
                borderRadius: '10px'
              }}
            />
          )}
        </div>
        <div className="sla-progress-text">
          {timer.slaMinutos ? `${percentual.toFixed(1)}% do SLA utilizado` : 'Aguardando SLA...'}
        </div>
        <div className="sla-progress-legend">
          {etapas.ateDespacho > 0 && (
            <span className="sla-legend-item">
              <span className="sla-legend-color sla-legend-ate-despacho"></span>
              Até Despacho
            </span>
          )}
          {etapas.ateChegada > 0 && (
            <span className="sla-legend-item">
              <span className="sla-legend-color sla-legend-ida"></span>
              Deslocamento
            </span>
          )}
          {timer.foiConcluida && etapas.retorno > 0 && (
            <span className="sla-legend-item">
              <span className="sla-legend-color sla-legend-retorno"></span>
              Retorno (não conta para SLA)
            </span>
          )}
        </div>
      </div>

      <div className="sla-detalhes">
        <div className="sla-detalhe-item">
          <span className="sla-detalhe-label">Tempo Total:</span>
          <span className="sla-detalhe-valor">{timer.tempoTotalFormatado || '--'}</span>
        </div>
        {/* Mostrar tempo restante até chegada quando está indo (verde) OU tempo restante de retorno quando está voltando (amarelo) */}
        {!timer.foiConcluida && timer.tempoRestanteAteChegadaMinutos !== null && timer.tempoRestanteAteChegadaMinutos !== undefined && !timer.chegouLocal && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">Tempo Restante até Chegada:</span>
            <span className="sla-detalhe-valor" style={{ 
              color: timer.tempoRestanteAteChegadaMinutos <= 0 ? '#dc3545' : '#28a745',
              fontWeight: 'bold'
            }}>
              {timer.tempoRestanteAteChegadaMinutos > 0 ? `${timer.tempoRestanteAteChegadaMinutos}m` : 'Chegou!'}
            </span>
          </div>
        )}
        {/* Quando está retornando: mostrar "Tempo até Chegada" em amarelo com tempo decrescente de retorno */}
        {timer.foiConcluida && !timer.retornouBase && timer.tempoRestanteRetornoMinutos !== null && timer.tempoRestanteRetornoMinutos !== undefined && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">Tempo Restante até Chegada:</span>
            <span className="sla-detalhe-valor" style={{ 
              color: '#ffc107',
              fontWeight: 'bold'
            }}>
              {timer.tempoRestanteRetornoMinutos > 0 ? `${timer.tempoRestanteRetornoMinutos}m` : 'Retornou!'}
            </span>
          </div>
        )}
        {/* Quando chegou mas ainda não concluiu: mostrar tempo fixo */}
        {timer.tempoAteChegadaMinutos !== null && timer.chegouLocal && !timer.foiConcluida && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">Tempo até Chegada:</span>
            <span className="sla-detalhe-valor">
              {timer.tempoAteChegadaMinutos !== null ? `${timer.tempoAteChegadaMinutos}m` : '--'}
            </span>
          </div>
        )}
        {timer.distanciaKm !== null && timer.distanciaKm !== undefined && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">Distância:</span>
            <span className="sla-detalhe-valor">
              {timer.distanciaKm.toFixed(2)} km
            </span>
          </div>
        )}
        {timer.slaMinutos && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">SLA Total:</span>
            <span className="sla-detalhe-valor">{timer.slaMinutos}m</span>
          </div>
        )}
        {timer.foiConcluida && timer.retornouBase && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">Tempo Retorno:</span>
            <span className="sla-detalhe-valor" style={{ color: '#28a745' }}>
              {timer.tempoRetornoDecorridoMinutos !== null ? `${timer.tempoRetornoDecorridoMinutos}m` : '--'} ✅
            </span>
          </div>
        )}
        {timer.foiConcluida && timer.retornouBase && (
          <div className="sla-detalhe-item">
            <span className="sla-detalhe-label">Status:</span>
            <span className="sla-detalhe-valor" style={{ color: '#28a745', fontWeight: 'bold' }}>
              Ambulância e equipe disponíveis ✅
            </span>
          </div>
        )}
      </div>
    </div>
  );
}

export default SLATimer;
