import { useState, useEffect } from 'react';
import { equipeService } from '../../../services/equipeService';
import { ambulanciaService } from '../../../services/ambulanciaService';
import { profissionalService } from '../../../services/profissionalService';
import '../AdminDashboard.css';

function GerenciarEquipes() {
  const [equipes, setEquipes] = useState([]);
  const [ambulancias, setAmbulancias] = useState([]);
  const [profissionais, setProfissionais] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    idAmbulancia: '',
    descricao: '',
    idsProfissionais: [],
  });

  useEffect(() => {
    carregarDados();
  }, []);

  const carregarDados = async () => {
    try {
      setLoading(true);
      const [equipesData, ambData, profData] = await Promise.all([
        equipeService.listarEquipes(),
        ambulanciaService.listar(),
        profissionalService.listarDisponiveis(), // Só profissionais disponíveis
      ]);
      setEquipes(equipesData);
      setAmbulancias(ambData);
      setProfissionais(profData.filter(p => p.ativo && p.status === 'DISPONIVEL'));
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
        idAmbulancia: parseInt(formData.idAmbulancia),
        descricao: formData.descricao,
        idsProfissionais: formData.idsProfissionais.map(id => parseInt(id)),
      };
      await equipeService.criarEquipe(dados);
      handleCloseModal();
      carregarDados();
    } catch (err) {
      setError('Erro ao criar equipe: ' + err.message);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setFormData({ idAmbulancia: '', descricao: '', idsProfissionais: [] });
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
              </tr>
            </thead>
            <tbody>
              {equipes.length === 0 ? (
                <tr>
                  <td colSpan="5" className="empty-message">
                    Nenhuma equipe cadastrada
                  </td>
                </tr>
              ) : (
                equipes.map((equipe) => (
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
                      <span className={`status-badge ${equipe.ativa ? 'ativo' : 'inativo'}`}>
                        {equipe.ativa ? 'Ativa' : 'Inativa'}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Nova Equipe</h2>
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
              <div className="form-group">
                <label>Profissionais *</label>
                <div className="roles-checkbox" style={{ flexDirection: 'column', gap: '8px' }}>
                  {profissionais.map(prof => (
                    <label key={prof.id} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <input
                        type="checkbox"
                        checked={formData.idsProfissionais.includes(prof.id.toString())}
                        onChange={() => handleToggleProfissional(prof.id.toString())}
                      />
                      <span>
                        {prof.nome} - {prof.funcao}
                        {prof.turno && <span style={{ color: '#666', fontSize: '0.85rem' }}> ({prof.turno})</span>}
                      </span>
                    </label>
                  ))}
                </div>
                {profissionais.length === 0 && (
                  <small style={{ color: '#999' }}>Nenhum profissional disponível. Verifique se há profissionais DISPONÍVEIS e no mesmo turno.</small>
                )}
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

export default GerenciarEquipes;
