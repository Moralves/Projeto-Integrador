import { useState, useEffect } from 'react';
import { ocorrenciaService } from '../../../services/ocorrenciaService';
import '../OperatorLayout.css';

function SugerirAmbulancias({ ocorrenciaId, onDespachar, onClose }) {
  const [sugestoes, setSugestoes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [despachando, setDespachando] = useState(false);

  useEffect(() => {
    carregarSugestoes();
  }, [ocorrenciaId]);

  const carregarSugestoes = async () => {
    try {
      setLoading(true);
      setError('');
      const dados = await ocorrenciaService.sugerirAmbulancias(ocorrenciaId);
      setSugestoes(dados);
    } catch (err) {
      setError('Erro ao carregar sugestões: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDespachar = async () => {
    if (!window.confirm('Deseja realmente despachar esta ocorrência? O sistema escolherá automaticamente a melhor ambulância disponível.')) {
      return;
    }

    try {
      setDespachando(true);
      setError('');
      await ocorrenciaService.despachar(ocorrenciaId);
      alert('Ocorrência despachada com sucesso!');
      if (onDespachar) {
        onDespachar();
      }
      onClose();
    } catch (err) {
      setError('Erro ao despachar ocorrência: ' + err.message);
      alert('Erro ao despachar: ' + err.message);
    } finally {
      setDespachando(false);
    }
  };

  const getSlaBadge = (dentroSLA, slaMinutos) => {
    if (dentroSLA) {
      return {
        bg: '#d1fae5',
        color: '#065f46',
        text: `✅ Dentro do SLA (${slaMinutos} min)`
      };
    } else {
      return {
        bg: '#fee2e2',
        color: '#991b1b',
        text: `⚠️ Fora do SLA (${slaMinutos} min)`
      };
    }
  };

  const getTipoBadge = (tipo) => {
    const badges = {
      'BASICA': { bg: '#dbeafe', color: '#1e40af', text: '🚑 Básica' },
      'UTI': { bg: '#fee2e2', color: '#dc2626', text: '🚨 UTI' }
    };
    return badges[tipo] || { bg: '#f3f4f6', color: '#374151', text: tipo };
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: 'white',
          borderRadius: '12px',
          padding: '24px',
          maxWidth: '800px',
          width: '90%',
          maxHeight: '90vh',
          overflow: 'auto',
          boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h2 style={{ margin: 0, color: '#1f2937' }}>🔍 Ambulâncias Sugeridas</h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#6b7280',
              padding: '0',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            ×
          </button>
        </div>

        {error && (
          <div style={{
            padding: '12px 16px',
            backgroundColor: '#fee2e2',
            color: '#dc2626',
            borderRadius: '8px',
            marginBottom: '16px'
          }}>
            {error}
          </div>
        )}

        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
            Carregando sugestões...
          </div>
        ) : sugestoes.length === 0 ? (
          <div style={{
            padding: '40px',
            textAlign: 'center',
            color: '#6b7280'
          }}>
            <p style={{ marginBottom: '16px' }}>Nenhuma ambulância disponível encontrada.</p>
            <p style={{ fontSize: '0.9rem', color: '#9ca3af' }}>
              Verifique se há ambulâncias disponíveis com equipe completa.
            </p>
          </div>
        ) : (
          <>
            <div style={{
              marginBottom: '20px',
              padding: '16px',
              backgroundColor: '#f0f7ff',
              borderRadius: '8px',
              border: '1px solid #4a90e2'
            }}>
              <p style={{ margin: 0, color: '#4b5563', fontSize: '0.9rem', marginBottom: '8px' }}>
                <strong>💡 Informação:</strong> As ambulâncias são ordenadas por distância calculada pelo <strong>algoritmo Dijkstra</strong> (menor primeiro). 
                O sistema escolherá automaticamente a melhor opção ao despachar.
              </p>
              <p style={{ margin: 0, color: '#4b5563', fontSize: '0.85rem' }}>
                <strong>🧭 Sobre o Dijkstra:</strong> O algoritmo analisa todas as rotas possíveis na rede viária e seleciona o caminho mais curto 
                entre o bairro base da ambulância e o local da ocorrência, garantindo o menor tempo de resposta.
              </p>
            </div>

            <div style={{ marginBottom: '24px' }}>
              {sugestoes.map((sugestao, index) => {
                const slaBadge = getSlaBadge(sugestao.dentroSLA, sugestao.slaMinutos);
                const tipoBadge = getTipoBadge(sugestao.tipo);
                
                return (
                  <div
                    key={sugestao.id}
                    style={{
                      border: index === 0 ? '2px solid #10b981' : '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '16px',
                      marginBottom: '12px',
                      backgroundColor: index === 0 ? '#f0fdf4' : 'white'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '12px' }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                          <span style={{
                            padding: '4px 12px',
                            borderRadius: '12px',
                            fontSize: '0.875rem',
                            fontWeight: '600',
                            backgroundColor: tipoBadge.bg,
                            color: tipoBadge.color
                          }}>
                            {tipoBadge.text}
                          </span>
                          <span style={{ fontWeight: '700', fontSize: '1.1rem', color: '#1f2937' }}>
                            {sugestao.placa}
                          </span>
                          {index === 0 && (
                            <span style={{
                              padding: '4px 8px',
                              borderRadius: '6px',
                              fontSize: '0.75rem',
                              fontWeight: '600',
                              backgroundColor: '#10b981',
                              color: 'white'
                            }}>
                              🥇 Recomendada
                            </span>
                          )}
                        </div>
                        <div style={{ color: '#6b7280', fontSize: '0.9rem', marginBottom: '8px' }}>
                          📍 Bairro Base: <strong>{sugestao.bairroBase}</strong>
                        </div>
                      </div>
                    </div>

                    <div style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                      gap: '12px',
                      marginBottom: '12px'
                    }}>
                      <div>
                        <div style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '4px' }}>Distância</div>
                        <div style={{ fontWeight: '600', color: '#374151' }}>
                          📏 {sugestao.distanciaKm.toFixed(2)} km
                        </div>
                      </div>
                      <div>
                        <div style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '4px' }}>Tempo Estimado</div>
                        <div style={{ fontWeight: '600', color: '#374151' }}>
                          ⏱️ {sugestao.tempoEstimadoMinutos} min
                        </div>
                      </div>
                      <div>
                        <div style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '4px' }}>Status SLA</div>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '6px',
                          fontSize: '0.75rem',
                          fontWeight: '600',
                          backgroundColor: slaBadge.bg,
                          color: slaBadge.color
                        }}>
                          {slaBadge.text}
                        </span>
                      </div>
                      <div>
                        <div style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '4px' }}>Equipe</div>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '6px',
                          fontSize: '0.75rem',
                          fontWeight: '600',
                          backgroundColor: sugestao.equipeCompleta ? '#d1fae5' : '#fee2e2',
                          color: sugestao.equipeCompleta ? '#065f46' : '#991b1b'
                        }}>
                          {sugestao.statusEquipe}
                        </span>
                      </div>
                    </div>

                    <div style={{
                      marginTop: '12px',
                      padding: '12px',
                      backgroundColor: index === 0 ? '#ecfdf5' : '#f9fafb',
                      borderRadius: '6px',
                      border: index === 0 ? '1px solid #10b981' : '1px solid #e5e7eb'
                    }}>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '4px', fontWeight: '600' }}>
                        🧭 Justificativa (Algoritmo Dijkstra):
                      </div>
                      <div style={{ fontSize: '0.85rem', color: '#374151', lineHeight: '1.5' }}>
                        {index === 0 ? (
                          <strong style={{ color: '#059669' }}>
                            Esta é a ambulância mais próxima calculada pelo algoritmo Dijkstra, 
                            encontrando o caminho mais curto através da rede viária. 
                            Com {sugestao.distanciaKm.toFixed(2)} km de distância e tempo estimado de {sugestao.tempoEstimadoMinutos} minutos, 
                            {sugestao.dentroSLA ? ' está dentro do SLA' : ' pode atender dentro do prazo'} 
                            de {sugestao.slaMinutos} minutos para esta ocorrência.
                          </strong>
                        ) : (
                          <>
                            Ambulância posicionada a {sugestao.distanciaKm.toFixed(2)} km do local da ocorrência, 
                            calculado pelo algoritmo Dijkstra que encontra o menor caminho na rede viária. 
                            Tempo estimado: {sugestao.tempoEstimadoMinutos} minutos 
                            {sugestao.dentroSLA ? ' (dentro do SLA)' : ' (fora do SLA)'}.
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
              <button
                onClick={onClose}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#f3f4f6',
                  color: '#374151',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '0.95rem',
                  fontWeight: '600',
                  cursor: 'pointer'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleDespachar}
                disabled={despachando || sugestoes.length === 0}
                style={{
                  padding: '10px 20px',
                  backgroundColor: despachando || sugestoes.length === 0 ? '#9ca3af' : '#10b981',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '0.95rem',
                  fontWeight: '600',
                  cursor: despachando || sugestoes.length === 0 ? 'not-allowed' : 'pointer',
                  opacity: despachando || sugestoes.length === 0 ? 0.6 : 1
                }}
              >
                {despachando ? 'Despachando...' : '🚑 Despachar Ocorrência'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default SugerirAmbulancias;

