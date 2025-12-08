import { useState, useEffect } from 'react';
import { ambulanciaService } from '../../../services/ambulanciaService';
import { bairroService } from '../../../services/bairroService';
import { analiseEstrategicaService } from '../../../services/analiseEstrategicaService';
import AutocompleteSelect from '../../../components/AutocompleteSelect';
import '../AdminDashboard.css';

function GerenciarAmbulancias() {
  const [ambulancias, setAmbulancias] = useState([]);
  const [bairros, setBairros] = useState([]);
  const [bairrosSugeridos, setBairrosSugeridos] = useState([]);
  const [loadingSugestoes, setLoadingSugestoes] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [ambulanciasEmAtendimento, setAmbulanciasEmAtendimento] = useState(new Set());
  const [formData, setFormData] = useState({
    placa: '',
    tipo: 'BASICA',
    idBairroBase: '',
  });

  useEffect(() => {
    carregarAmbulancias();
    carregarBairros();
  }, []);

  const carregarBairros = async () => {
    try {
      const dados = await bairroService.listar();
      setBairros(dados);
    } catch (err) {
      console.error('Erro ao carregar bairros:', err);
    }
  };

  const carregarAmbulancias = async () => {
    try {
      setLoading(true);
      const data = await ambulanciaService.listar();
      setAmbulancias(data);
      
      // Verificar quais ambulâncias estão em atendimento
      const emAtendimentoSet = new Set();
      for (const amb of data) {
        try {
          const emAtendimento = await ambulanciaService.verificarEmAtendimento(amb.id);
          if (emAtendimento) {
            emAtendimentoSet.add(amb.id);
          }
        } catch (err) {
          console.error(`Erro ao verificar atendimento da ambulância ${amb.id}:`, err);
        }
      }
      setAmbulanciasEmAtendimento(emAtendimentoSet);
      
      setError('');
    } catch (err) {
      setError('Erro ao carregar ambulâncias: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      
      // Validar se bairro foi selecionado
      if (!formData.idBairroBase || formData.idBairroBase === '') {
        setError('Selecione um bairro base');
        return;
      }

      const dadosEnvio = {
        placa: formData.placa.trim(),
        tipo: formData.tipo,
        idBairroBase: parseInt(formData.idBairroBase)
      };

      await ambulanciaService.cadastrar(dadosEnvio);
      handleCloseModal();
      carregarAmbulancias();
    } catch (err) {
      setError('Erro ao salvar ambulância: ' + err.message);
    }
  };

  const handleOpenModal = async () => {
    setShowModal(true);
    setBairrosSugeridos([]);
    setLoadingSugestoes(true);
    try {
      // Passar o tipo de ambulância selecionado para análise estratégica
      const tipoAmbulancia = formData.tipo || null;
      const sugestoes = await analiseEstrategicaService.obterBairrosSugeridos(tipoAmbulancia);
      setBairrosSugeridos(sugestoes.slice(0, 5)); // Top 5 sugestões
    } catch (err) {
      console.error('Erro ao carregar sugestões:', err);
      // Não mostrar erro, apenas não exibir sugestões
    } finally {
      setLoadingSugestoes(false);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setFormData({ placa: '', tipo: 'BASICA', idBairroBase: '' });
    setBairrosSugeridos([]);
  };

  const handleSelecionarSugestao = (bairroId) => {
    setFormData({ ...formData, idBairroBase: bairroId.toString() });
  };

  const handleTipoChange = async (e) => {
    const novoTipo = e.target.value;
    setFormData({ ...formData, tipo: novoTipo, idBairroBase: '' });
    
    // Se o modal estiver aberto, recarregar sugestões com o novo tipo
    if (showModal) {
      setBairrosSugeridos([]);
      setLoadingSugestoes(true);
      try {
        const sugestoes = await analiseEstrategicaService.obterBairrosSugeridos(novoTipo);
        setBairrosSugeridos(sugestoes.slice(0, 5)); // Top 5 sugestões
      } catch (err) {
        console.error('Erro ao carregar sugestões:', err);
      } finally {
        setLoadingSugestoes(false);
      }
    }
  };

  const handleToggleStatus = async (id, ativa) => {
    try {
      if (ambulanciasEmAtendimento.has(id)) {
        setError('Não é possível alterar status de ambulância em atendimento. Finalize o atendimento antes.');
        return;
      }
      
      if (ativa) {
        await ambulanciaService.desativar(id);
      } else {
        await ambulanciaService.ativar(id);
      }
      carregarAmbulancias();
    } catch (err) {
      setError(err.message || 'Erro ao alterar status: ' + err.message);
    }
  };

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Gerenciamento de Ambulâncias</h1>
        <button className="btn-primary" onClick={handleOpenModal}>
          + Nova Ambulância
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Carregando ambulâncias...</div>
      ) : (
        <div className="usuarios-table-container">
          <table className="usuarios-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Placa</th>
                <th>Tipo</th>
                <th>Status</th>
                <th>Bairro Base</th>
                <th>Ativa</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {ambulancias.length === 0 ? (
                <tr>
                  <td colSpan="7" className="empty-message">
                    Nenhuma ambulância cadastrada
                  </td>
                </tr>
              ) : (
                ambulancias.map((amb) => {
                  const emAtendimento = ambulanciasEmAtendimento.has(amb.id) || amb.status === 'EM_ATENDIMENTO';
                  return (
                    <tr key={amb.id}>
                      <td>{amb.id}</td>
                      <td>{amb.placa}</td>
                      <td>{amb.tipo}</td>
                      <td>
                        <span className={`status-badge ${amb.status === 'EM_ATENDIMENTO' ? 'em-atendimento' : 'ativo'}`}>
                          {amb.status}
                          {emAtendimento && ' 🚨'}
                        </span>
                      </td>
                      <td>{amb.bairroBase?.nome || 'N/A'}</td>
                      <td>
                        <span className={`status-badge ${amb.ativa ? 'ativo' : 'inativo'}`}>
                          {amb.ativa ? 'Sim' : 'Não'}
                        </span>
                      </td>
                      <td className="actions">
                        <button
                          className={`btn-toggle ${amb.ativa ? 'desativar' : 'ativar'}`}
                          onClick={() => handleToggleStatus(amb.id, amb.ativa)}
                          disabled={emAtendimento}
                          style={{
                            opacity: emAtendimento ? 0.5 : 1,
                            cursor: emAtendimento ? 'not-allowed' : 'pointer'
                          }}
                          title={emAtendimento ? 'Não é possível alterar status de ambulância em atendimento. Finalize o atendimento antes.' : ''}
                        >
                          {amb.ativa ? 'Desativar' : 'Ativar'}
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Nova Ambulância</h2>
              <button className="btn-close" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Placa *</label>
                <input
                  type="text"
                  value={formData.placa}
                  onChange={(e) => setFormData({ ...formData, placa: e.target.value })}
                  required
                  placeholder="ABC-1234"
                />
              </div>
              <div className="form-group">
                <label>Tipo *</label>
                <select
                  value={formData.tipo}
                  onChange={handleTipoChange}
                  required
                >
                  <option value="BASICA">Básica</option>
                  <option value="UTI">UTI</option>
                </select>
              </div>
              <div className="form-group">
                <label>Bairro Base *</label>
                {bairrosSugeridos.length > 0 && (
                  <div style={{ 
                    marginBottom: '12px', 
                    padding: '12px', 
                    backgroundColor: '#f0f7ff', 
                    borderRadius: '6px',
                    border: '1px solid #4a90e2'
                  }}>
                    <div style={{ 
                      fontSize: '0.9rem', 
                      fontWeight: '600', 
                      color: '#2c5aa0',
                      marginBottom: '8px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}>
                      <span>💡</span>
                      <span>Sugestões baseadas no algoritmo Dijkstra e análise de ocorrências:</span>
                    </div>
                    <div style={{ 
                      fontSize: '0.8rem', 
                      color: '#4b5563',
                      marginBottom: '8px',
                      paddingLeft: '24px',
                      fontStyle: 'italic'
                    }}>
                      Análise estratégica considera: ocorrências relevantes para {formData.tipo === 'UTI' ? 'UTI' : 'Básica'}, 
                      distância mínima para outras ambulâncias (evita aglomeração), tempo médio de resposta calculado pelo Dijkstra, 
                      e distribuição geográfica equilibrada para máxima eficiência.
                    </div>
                    {loadingSugestoes ? (
                      <div style={{ color: '#666', fontSize: '0.85rem' }}>Carregando sugestões...</div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        {bairrosSugeridos.map((sugestao, idx) => (
                          <button
                            key={sugestao.id}
                            type="button"
                            onClick={() => handleSelecionarSugestao(sugestao.id)}
                            style={{
                              textAlign: 'left',
                              padding: '8px 12px',
                              backgroundColor: formData.idBairroBase === sugestao.id.toString() ? '#4a90e2' : '#fff',
                              color: formData.idBairroBase === sugestao.id.toString() ? '#fff' : '#333',
                              border: `1px solid ${formData.idBairroBase === sugestao.id.toString() ? '#4a90e2' : '#ddd'}`,
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '0.85rem',
                              transition: 'all 0.2s',
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center'
                            }}
                            onMouseEnter={(e) => {
                              if (formData.idBairroBase !== sugestao.id.toString()) {
                                e.target.style.backgroundColor = '#e8f4fd';
                              }
                            }}
                            onMouseLeave={(e) => {
                              if (formData.idBairroBase !== sugestao.id.toString()) {
                                e.target.style.backgroundColor = '#fff';
                              }
                            }}
                          >
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600' }}>
                                {idx + 1}. {sugestao.nome}
                              </div>
                              <div style={{ 
                                fontSize: '0.75rem', 
                                opacity: 0.8,
                                marginTop: '2px'
                              }}>
                                {sugestao.justificativa}
                              </div>
                              <div style={{ 
                                fontSize: '0.7rem', 
                                marginTop: '4px',
                                display: 'flex',
                                gap: '12px',
                                opacity: 0.7,
                                flexWrap: 'wrap'
                              }}>
                                {sugestao.bairrosAlcancaveis !== undefined && sugestao.bairrosAlcancaveis > 0 && (
                                  <span style={{ 
                                    fontWeight: '600', 
                                    color: sugestao.bairrosAlcancaveis >= 10 ? '#059669' : '#6b7280'
                                  }}>
                                    🔗 {sugestao.bairrosAlcancaveis} bairro(s) alcançável(is) via Dijkstra
                                  </span>
                                )}
                                {sugestao.ocorrenciasNoBairro > 0 && (
                                  <span>📊 {sugestao.ocorrenciasNoBairro} ocorrência(s)</span>
                                )}
                                {sugestao.tempoMedioResposta > 0 && (
                                  <span>⏱️ {sugestao.tempoMedioResposta.toFixed(1)} min médio</span>
                                )}
                                {sugestao.ambulanciasExistentes > 0 && (
                                  <span>🚑 {sugestao.ambulanciasExistentes} ambulância(s)</span>
                                )}
                              </div>
                            </div>
                            {formData.idBairroBase === sugestao.id.toString() && (
                              <span style={{ marginLeft: '8px', fontSize: '1.2rem' }}>✓</span>
                            )}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                )}
                <AutocompleteSelect
                  options={bairros}
                  value={formData.idBairroBase}
                  onChange={(value) => setFormData({ ...formData, idBairroBase: value })}
                  placeholder="Digite ou selecione o bairro base..."
                  getOptionLabel={(opt) => opt.nome}
                  getOptionValue={(opt) => opt.id.toString()}
                  required={true}
                />
                <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '4px', display: 'block' }}>
                  Bairro onde a ambulância está estacionada (ponto de partida)
                </small>
              </div>
              {error && <div className="form-error">{error}</div>}
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">Criar</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default GerenciarAmbulancias;
