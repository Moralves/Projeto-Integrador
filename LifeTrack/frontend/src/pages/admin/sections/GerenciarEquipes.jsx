import { useState, useEffect } from 'react';
import { equipeService } from '../../../services/equipeService';
import { ambulanciaService } from '../../../services/ambulanciaService';
import { profissionalService } from '../../../services/profissionalService';
import '../AdminDashboard.css';

function GerenciarEquipes() {
  const [equipes, setEquipes] = useState([]);
  const [equipesStatus, setEquipesStatus] = useState({}); // { equipeId: boolean (emAtendimento) }
  const [ambulancias, setAmbulancias] = useState([]);
  const [profissionais, setProfissionais] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingEquipe, setEditingEquipe] = useState(null);
  const [formData, setFormData] = useState({
    idAmbulancia: '',
    descricao: '',
    idsProfissionais: [],
  });

  useEffect(() => {
    carregarDados();
  }, []);

  const carregarDados = async (incluirTodosProfissionais = false) => {
    try {
      setLoading(true);
      const [equipesData, ambData] = await Promise.all([
        equipeService.listarEquipes(),
        ambulanciaService.listar(),
      ]);
      
      // Carregar profissionais: todos se estiver editando, só disponíveis se não
      let profData;
      if (incluirTodosProfissionais) {
        profData = await profissionalService.listar(); // Todos os profissionais
      } else {
        profData = await profissionalService.listarDisponiveis(); // Só disponíveis
      }
      
      setEquipes(equipesData);
      setAmbulancias(ambData);
      
      // Filtrar profissionais: remover os que já estão em outras equipes ativas
      // (exceto quando estiver editando, onde precisamos mostrar os que já estão na equipe atual)
      let profissionaisFiltrados = profData.filter(p => p.ativo);
      
      if (!incluirTodosProfissionais) {
        // Na criação, remover profissionais que já estão em outras equipes
        const idsProfissionaisEmEquipes = new Set();
        equipesData.forEach(equipe => {
          if (equipe.ativa && equipe.profissionais) {
            equipe.profissionais.forEach(ep => {
              if (ep.profissional?.id) {
                idsProfissionaisEmEquipes.add(ep.profissional.id);
              }
            });
          }
        });
        
        profissionaisFiltrados = profissionaisFiltrados.filter(p => 
          !idsProfissionaisEmEquipes.has(p.id)
        );
      } else {
        // Na edição, mostrar todos os profissionais ativos (incluindo os que já estão na equipe atual)
        profissionaisFiltrados = profData.filter(p => p.ativo);
      }
      
      setProfissionais(profissionaisFiltrados);
      
      // Carregar status de cada equipe
      const statusMap = {};
      for (const equipe of equipesData) {
        try {
          const emAtendimento = await equipeService.verificarStatus(equipe.id);
          statusMap[equipe.id] = emAtendimento;
        } catch (err) {
          statusMap[equipe.id] = false;
        }
      }
      setEquipesStatus(statusMap);
      
      setError('');
    } catch (err) {
      setError('Erro ao carregar dados: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      const dados = {
        descricao: formData.descricao,
        idsProfissionais: formData.idsProfissionais.map(id => parseInt(id)),
      };
      
      if (editingEquipe) {
        // Atualizar equipe existente
        await equipeService.atualizarEquipe(editingEquipe.id, dados);
      } else {
        // Criar nova equipe
        dados.idAmbulancia = parseInt(formData.idAmbulancia);
        await equipeService.criarEquipe(dados);
      }
      
      handleCloseModal();
      carregarDados();
    } catch (err) {
      setError('Erro ao ' + (editingEquipe ? 'atualizar' : 'criar') + ' equipe: ' + err.message);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingEquipe(null);
    setFormData({ idAmbulancia: '', descricao: '', idsProfissionais: [] });
  };

  const handleEdit = async (equipe) => {
    setEditingEquipe(equipe);
    setFormData({
      idAmbulancia: equipe.ambulancia?.id?.toString() || '',
      descricao: equipe.descricao || '',
      idsProfissionais: equipe.profissionais?.map(ep => ep.profissional?.id?.toString()).filter(Boolean) || [],
    });
    // Recarregar dados incluindo todos os profissionais (para permitir manter os que já estão na equipe)
    await carregarDados(true);
    setShowModal(true);
  };

  const handleToggleProfissional = (id) => {
    const ids = formData.idsProfissionais || [];
    if (ids.includes(id)) {
      setFormData({ ...formData, idsProfissionais: ids.filter(i => i !== id) });
    } else {
      setFormData({ ...formData, idsProfissionais: [...ids, id] });
    }
  };

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Gerenciamento de Equipes</h1>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          + Nova Equipe
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Carregando equipes...</div>
      ) : (
        <div className="usuarios-table-container">
          <table className="usuarios-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Descrição</th>
                <th>Ambulância</th>
                <th>Profissionais</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {equipes.length === 0 ? (
                <tr>
                  <td colSpan="6" className="empty-message">
                    Nenhuma equipe cadastrada
                  </td>
                </tr>
              ) : (
                equipes.map((equipe) => {
                  const emAtendimento = equipesStatus[equipe.id] || false;
                  const podeEditar = equipe.ativa && !emAtendimento;
                  
                  return (
                    <tr key={equipe.id}>
                      <td>{equipe.id}</td>
                      <td>{equipe.descricao}</td>
                      <td>{equipe.ambulancia?.placa || 'N/A'}</td>
                      <td>
                        {equipe.profissionais?.length > 0
                          ? equipe.profissionais.map(ep => ep.profissional?.nome).join(', ')
                          : 'Sem profissionais'}
                      </td>
                      <td>
                        {emAtendimento ? (
                          <span className="status-badge" style={{ backgroundColor: '#dc3545', color: 'white' }}>
                            Em Atendimento
                          </span>
                        ) : equipe.ativa ? (
                          <span className="status-badge" style={{ backgroundColor: '#28a745', color: 'white' }}>
                            Disponível
                          </span>
                        ) : (
                          <span className="status-badge inativo">
                            Inativa
                          </span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn-secondary"
                          onClick={() => handleEdit(equipe)}
                          disabled={!podeEditar}
                          title={!podeEditar ? (emAtendimento ? 'Equipe em atendimento não pode ser editada' : 'Equipe inativa não pode ser editada') : 'Editar equipe'}
                          style={{
                            padding: '6px 12px',
                            fontSize: '0.875rem',
                            opacity: podeEditar ? 1 : 0.5,
                            cursor: podeEditar ? 'pointer' : 'not-allowed'
                          }}
                        >
                          ✏️ Editar
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
              <h2>{editingEquipe ? 'Editar Equipe' : 'Nova Equipe'}</h2>
              <button className="btn-close" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Descrição *</label>
                <input
                  type="text"
                  value={formData.descricao}
                  onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
                  required
                  placeholder="Equipe Alpha"
                />
              </div>
              {!editingEquipe && (
                <div className="form-group">
                  <label>Ambulância *</label>
                  <select
                    value={formData.idAmbulancia}
                    onChange={(e) => setFormData({ ...formData, idAmbulancia: e.target.value })}
                    required
                  >
                    <option value="">Selecione uma ambulância</option>
                    {ambulancias
                      .filter(a => a.ativa && a.status === 'DISPONIVEL')
                      .map(amb => (
                        <option key={amb.id} value={amb.id}>
                          {amb.placa} - {amb.tipo}
                        </option>
                      ))}
                  </select>
                </div>
              )}
              {editingEquipe && (
                <div className="form-group">
                  <label>Ambulância</label>
                  <input
                    type="text"
                    value={editingEquipe.ambulancia?.placa || 'N/A'}
                    disabled
                    style={{ backgroundColor: '#f5f5f5', cursor: 'not-allowed' }}
                  />
                  <small style={{ color: '#6c757d', display: 'block', marginTop: '4px' }}>
                    A ambulância não pode ser alterada após a criação da equipe
                  </small>
                </div>
              )}
              <div className="form-group">
                <label>Profissionais *</label>
                <div className="profissionais-select-container">
                  {profissionais.length === 0 ? (
                    <div className="profissionais-empty">
                      <p>Nenhum profissional disponível</p>
                      <small>Verifique se há profissionais DISPONÍVEIS e no mesmo turno.</small>
                    </div>
                  ) : (
                    <div className="profissionais-grid">
                      {profissionais.map(prof => {
                        const isSelected = formData.idsProfissionais.includes(prof.id.toString());
                        const isNaEquipe = editingEquipe && editingEquipe.profissionais?.some(
                          ep => ep.profissional?.id === prof.id
                        );
                        const podeSelecionar = !editingEquipe || prof.status === 'DISPONIVEL' || isNaEquipe;
                        
                        const getFuncaoColor = (funcao) => {
                          const funcaoUpper = funcao?.toUpperCase() || '';
                          if (funcaoUpper.includes('MEDICO')) return '#dc3545';
                          if (funcaoUpper.includes('ENFERMEIRO')) return '#17a2b8';
                          if (funcaoUpper.includes('CONDUTOR')) return '#28a745';
                          return '#6c757d';
                        };
                        const getTurnoColor = (turno) => {
                          const turnoUpper = turno?.toUpperCase() || '';
                          if (turnoUpper.includes('MANHA')) return '#ffc107';
                          if (turnoUpper.includes('TARDE')) return '#fd7e14';
                          if (turnoUpper.includes('NOITE')) return '#6f42c1';
                          return '#6c757d';
                        };
                        return (
                          <div
                            key={prof.id}
                            className={`profissional-card ${isSelected ? 'selected' : ''} ${!podeSelecionar ? 'disabled' : ''}`}
                            onClick={() => podeSelecionar && handleToggleProfissional(prof.id.toString())}
                            style={{
                              opacity: podeSelecionar ? 1 : 0.6,
                              cursor: podeSelecionar ? 'pointer' : 'not-allowed'
                            }}
                            title={!podeSelecionar ? 'Profissional não disponível para esta equipe' : ''}
                          >
                            <div className="profissional-card-header">
                              <div className="profissional-checkbox">
                                <input
                                  type="checkbox"
                                  checked={isSelected}
                                  disabled={!podeSelecionar}
                                  onChange={() => podeSelecionar && handleToggleProfissional(prof.id.toString())}
                                  onClick={(e) => e.stopPropagation()}
                                />
                              </div>
                              <div className="profissional-name">
                                {prof.nome}
                                {isNaEquipe && editingEquipe && (
                                  <span style={{ fontSize: '0.75rem', color: '#17a2b8', marginLeft: '4px' }}>
                                    (na equipe)
                                  </span>
                                )}
                              </div>
                            </div>
                            <div className="profissional-details">
                              <span 
                                className="profissional-funcao"
                                style={{ backgroundColor: getFuncaoColor(prof.funcao) }}
                              >
                                {prof.funcao}
                              </span>
                              {prof.turno && (
                                <span 
                                  className="profissional-turno"
                                  style={{ backgroundColor: getTurnoColor(prof.turno) }}
                                >
                                  {prof.turno}
                                </span>
                              )}
                              {prof.status && (
                                <span 
                                  className="profissional-status"
                                  style={{ 
                                    backgroundColor: prof.status === 'DISPONIVEL' ? '#28a745' : '#6c757d',
                                    fontSize: '0.7rem',
                                    padding: '2px 6px',
                                    borderRadius: '4px',
                                    color: 'white'
                                  }}
                                >
                                  {prof.status}
                                </span>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
                {formData.idsProfissionais.length > 0 && (
                  <div className="profissionais-selected-count">
                    {formData.idsProfissionais.length} profissional{formData.idsProfissionais.length !== 1 ? 'is' : ''} selecionado{formData.idsProfissionais.length !== 1 ? 's' : ''}
                  </div>
                )}
              </div>
              {error && <div className="form-error">{error}</div>}
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  {editingEquipe ? 'Salvar Alterações' : 'Criar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default GerenciarEquipes;
