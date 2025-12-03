import { useState, useEffect } from 'react';
import { ambulanciaService } from '../../../services/ambulanciaService';
import '../AdminDashboard.css';

function GerenciarAmbulancias() {
  const [ambulancias, setAmbulancias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    placa: '',
    tipo: 'BASICA',
    idBairroBase: null,
  });

  useEffect(() => {
    carregarAmbulancias();
  }, []);

  const carregarAmbulancias = async () => {
    try {
      setLoading(true);
      const data = await ambulanciaService.listar();
      setAmbulancias(data);
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
      await ambulanciaService.cadastrar(formData);
      handleCloseModal();
      carregarAmbulancias();
    } catch (err) {
      setError('Erro ao salvar ambulância: ' + err.message);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setFormData({ placa: '', tipo: 'BASICA', idBairroBase: null });
  };

  const handleToggleStatus = async (id, ativa) => {
    try {
      if (ativa) {
        await ambulanciaService.desativar(id);
      } else {
        await ambulanciaService.ativar(id);
      }
      carregarAmbulancias();
    } catch (err) {
      setError('Erro ao alterar status: ' + err.message);
    }
  };

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Gerenciamento de Ambulâncias</h1>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
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
                ambulancias.map((amb) => (
                  <tr key={amb.id}>
                    <td>{amb.id}</td>
                    <td>{amb.placa}</td>
                    <td>{amb.tipo}</td>
                    <td>
                      <span className="status-badge ativo">{amb.status}</span>
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
                      >
                        {amb.ativa ? 'Desativar' : 'Ativar'}
                      </button>
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
                  onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                  required
                >
                  <option value="BASICA">Básica</option>
                  <option value="UTI">UTI</option>
                </select>
              </div>
              <div className="form-group">
                <label>ID Bairro Base *</label>
                <input
                  type="number"
                  value={formData.idBairroBase || ''}
                  onChange={(e) => setFormData({ ...formData, idBairroBase: parseInt(e.target.value) })}
                  required
                  placeholder="1"
                />
                <small style={{ color: '#666', fontSize: '0.85rem' }}>
                  Nota: Você precisa criar bairros primeiro no banco de dados
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
