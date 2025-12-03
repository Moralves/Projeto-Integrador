import { useState, useEffect } from 'react';
import { profissionalService } from '../../../services/profissionalService';
import '../AdminDashboard.css';

function GerenciarFuncionarios() {
  const [profissionais, setProfissionais] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    nome: '',
    funcao: 'MEDICO',
    contato: '',
  });

  useEffect(() => {
    carregarProfissionais();
  }, []);

  const carregarProfissionais = async () => {
    try {
      setLoading(true);
      const data = await profissionalService.listar();
      setProfissionais(data);
      setError('');
    } catch (err) {
      setError('Erro ao carregar profissionais: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      await profissionalService.cadastrar(formData);
      handleCloseModal();
      carregarProfissionais();
    } catch (err) {
      setError('Erro ao salvar profissional: ' + err.message);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setFormData({ nome: '', funcao: 'MEDICO', contato: '' });
  };

  const handleDesativar = async (id) => {
    if (!window.confirm('Tem certeza que deseja desativar este profissional?')) {
      return;
    }
    try {
      await profissionalService.desativar(id);
      carregarProfissionais();
    } catch (err) {
      setError('Erro ao desativar profissional: ' + err.message);
    }
  };

  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Gerenciamento de Funcionários</h1>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          + Novo Funcionário
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Carregando profissionais...</div>
      ) : (
        <div className="usuarios-table-container">
          <table className="usuarios-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Função</th>
                <th>Contato</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {profissionais.length === 0 ? (
                <tr>
                  <td colSpan="6" className="empty-message">
                    Nenhum profissional cadastrado
                  </td>
                </tr>
              ) : (
                profissionais.map((prof) => (
                  <tr key={prof.id}>
                    <td>{prof.id}</td>
                    <td>{prof.nome}</td>
                    <td>
                      <span className="role-badge">{prof.funcao}</span>
                    </td>
                    <td>{prof.contato || '-'}</td>
                    <td>
                      <span className={`status-badge ${prof.ativo ? 'ativo' : 'inativo'}`}>
                        {prof.ativo ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td className="actions">
                      {prof.ativo && (
                        <button
                          className="btn-toggle desativar"
                          onClick={() => handleDesativar(prof.id)}
                        >
                          Desativar
                        </button>
                      )}
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
              <h2>Novo Funcionário</h2>
              <button className="btn-close" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Nome Completo *</label>
                <input
                  type="text"
                  value={formData.nome}
                  onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                  required
                  placeholder="Dr. João Silva"
                />
              </div>
              <div className="form-group">
                <label>Função *</label>
                <select
                  value={formData.funcao}
                  onChange={(e) => setFormData({ ...formData, funcao: e.target.value })}
                  required
                >
                  <option value="MEDICO">Médico</option>
                  <option value="ENFERMEIRO">Enfermeiro</option>
                  <option value="CONDUTOR">Condutor</option>
                </select>
              </div>
              <div className="form-group">
                <label>Contato</label>
                <input
                  type="text"
                  value={formData.contato}
                  onChange={(e) => setFormData({ ...formData, contato: e.target.value })}
                  placeholder="(11) 99999-1111"
                />
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

export default GerenciarFuncionarios;
