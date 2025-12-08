import { useState, useEffect } from 'react';
import { profissionalService } from '../../../services/profissionalService';
import '../AdminDashboard.css';

function GerenciarFuncionarios() {
  const [profissionais, setProfissionais] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [filtroTurno, setFiltroTurno] = useState('');
  const [filtroStatus, setFiltroStatus] = useState('');
  const [formData, setFormData] = useState({
    nome: '',
    funcao: 'MEDICO',
    contato: '',
    turno: 'MANHA',
    status: 'DISPONIVEL',
    ativo: true,
  });

  useEffect(() => {
    carregarProfissionais();
  }, [filtroTurno, filtroStatus]);

  const carregarProfissionais = async () => {
    try {
      setLoading(true);
      const data = await profissionalService.listar(
        filtroTurno || null,
        filtroStatus || null
      );
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
      if (editingId) {
        await profissionalService.editar(editingId, formData);
      } else {
        await profissionalService.cadastrar(formData);
      }
      handleCloseModal();
      carregarProfissionais();
    } catch (err) {
      setError('Erro ao salvar profissional: ' + err.message);
    }
  };

  const handleEdit = async (id) => {
    try {
      const prof = await profissionalService.buscarPorId(id);
      
      // Verificar se o profissional está em atendimento
      if (prof.status === 'EM_ATENDIMENTO') {
        setError('Não é possível editar funcionário que está em atendimento. Finalize o atendimento antes de editar.');
        return;
      }
      
      setFormData({
        nome: prof.nome,
        funcao: prof.funcao,
        contato: prof.contato || '',
        turno: prof.turno || 'MANHA',
        status: prof.status || 'DISPONIVEL',
        ativo: prof.ativo !== undefined ? prof.ativo : true,
      });
      setEditingId(id);
      setShowModal(true);
    } catch (err) {
      setError('Erro ao carregar profissional: ' + err.message);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingId(null);
    setFormData({
      nome: '',
      funcao: 'MEDICO',
      contato: '',
      turno: 'MANHA',
      status: 'DISPONIVEL',
      ativo: true,
    });
  };

  const handleAlterarStatus = async (id, novoStatus) => {
    try {
      await profissionalService.alterarStatus(id, novoStatus);
      carregarProfissionais();
    } catch (err) {
      setError('Erro ao alterar status: ' + err.message);
    }
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

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'DISPONIVEL':
        return 'status-badge ativo';
      case 'EM_ATENDIMENTO':
        return 'status-badge em-atendimento';
      case 'EM_FOLGA':
        return 'status-badge em-folga';
      case 'INATIVO':
        return 'status-badge inativo';
      default:
        return 'status-badge';
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case 'DISPONIVEL':
        return 'Disponível';
      case 'EM_ATENDIMENTO':
        return 'Em Atendimento';
      case 'EM_FOLGA':
        return 'Em Folga';
      case 'INATIVO':
        return 'Inativo';
      default:
        return status;
    }
  };

  const getTurnoLabel = (turno) => {
    switch (turno) {
      case 'MANHA':
        return 'Manhã';
      case 'TARDE':
        return 'Tarde';
      case 'NOITE':
        return 'Noite';
      default:
        return turno;
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

      {/* Filtros */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '12px', alignItems: 'center' }}>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <label style={{ fontWeight: '600' }}>Filtros:</label>
          <select
            value={filtroTurno}
            onChange={(e) => setFiltroTurno(e.target.value)}
            style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ddd' }}
          >
            <option value="">Todos os Turnos</option>
            <option value="MANHA">Manhã</option>
            <option value="TARDE">Tarde</option>
            <option value="NOITE">Noite</option>
          </select>
          <select
            value={filtroStatus}
            onChange={(e) => setFiltroStatus(e.target.value)}
            style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ddd' }}
          >
            <option value="">Todos os Status</option>
            <option value="DISPONIVEL">Disponível</option>
            <option value="EM_ATENDIMENTO">Em Atendimento</option>
            <option value="EM_FOLGA">Em Folga</option>
            <option value="INATIVO">Inativo</option>
          </select>
        </div>
      </div>

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
                <th>Turno</th>
                <th>Status</th>
                <th>Contato</th>
                <th>Ativo</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {profissionais.length === 0 ? (
                <tr>
                  <td colSpan="8" className="empty-message">
                    Nenhum profissional encontrado
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
                    <td>{getTurnoLabel(prof.turno)}</td>
                    <td>
                      <span className={getStatusBadgeClass(prof.status)}>
                        {getStatusLabel(prof.status)}
                      </span>
                    </td>
                    <td>{prof.contato || '-'}</td>
                    <td>
                      <span className={`status-badge ${prof.ativo ? 'ativo' : 'inativo'}`}>
                        {prof.ativo ? 'Sim' : 'Não'}
                      </span>
                    </td>
                    <td className="actions" style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                      <button
                        className="btn-toggle editar"
                        onClick={() => handleEdit(prof.id)}
                        disabled={prof.status === 'EM_ATENDIMENTO'}
                        style={{ 
                          backgroundColor: prof.status === 'EM_ATENDIMENTO' ? '#6c757d' : '#4a90e2', 
                          color: 'white',
                          opacity: prof.status === 'EM_ATENDIMENTO' ? 0.6 : 1,
                          cursor: prof.status === 'EM_ATENDIMENTO' ? 'not-allowed' : 'pointer'
                        }}
                        title={prof.status === 'EM_ATENDIMENTO' ? 'Não é possível editar funcionário em atendimento' : 'Editar funcionário'}
                      >
                        Editar
                      </button>
                      {prof.status === 'DISPONIVEL' && (
                        <button
                          className="btn-toggle"
                          onClick={() => handleAlterarStatus(prof.id, 'EM_FOLGA')}
                          disabled={prof.status === 'EM_ATENDIMENTO'}
                          style={{ 
                            backgroundColor: '#f39c12', 
                            color: 'white', 
                            fontSize: '0.85rem',
                            opacity: prof.status === 'EM_ATENDIMENTO' ? 0.5 : 1,
                            cursor: prof.status === 'EM_ATENDIMENTO' ? 'not-allowed' : 'pointer'
                          }}
                          title={prof.status === 'EM_ATENDIMENTO' ? 'Não é possível alterar status de funcionário em atendimento' : ''}
                        >
                          Folga
                        </button>
                      )}
                      {prof.status === 'EM_FOLGA' && (
                        <button
                          className="btn-toggle"
                          onClick={() => handleAlterarStatus(prof.id, 'DISPONIVEL')}
                          disabled={prof.status === 'EM_ATENDIMENTO'}
                          style={{ 
                            backgroundColor: '#27ae60', 
                            color: 'white', 
                            fontSize: '0.85rem',
                            opacity: prof.status === 'EM_ATENDIMENTO' ? 0.5 : 1,
                            cursor: prof.status === 'EM_ATENDIMENTO' ? 'not-allowed' : 'pointer'
                          }}
                          title={prof.status === 'EM_ATENDIMENTO' ? 'Não é possível alterar status de funcionário em atendimento' : ''}
                        >
                          Disponível
                        </button>
                      )}
                      {prof.ativo && (
                        <button
                          className="btn-toggle desativar"
                          onClick={() => handleDesativar(prof.id)}
                          disabled={prof.status === 'EM_ATENDIMENTO'}
                          style={{
                            opacity: prof.status === 'EM_ATENDIMENTO' ? 0.5 : 1,
                            cursor: prof.status === 'EM_ATENDIMENTO' ? 'not-allowed' : 'pointer'
                          }}
                          title={prof.status === 'EM_ATENDIMENTO' ? 'Não é possível desativar funcionário em atendimento' : ''}
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
              <h2>{editingId ? 'Editar Funcionário' : 'Novo Funcionário'}</h2>
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
                <label>Turno *</label>
                <select
                  value={formData.turno}
                  onChange={(e) => setFormData({ ...formData, turno: e.target.value })}
                  required
                >
                  <option value="MANHA">Manhã</option>
                  <option value="TARDE">Tarde</option>
                  <option value="NOITE">Noite</option>
                </select>
              </div>
              <div className="form-group">
                <label>Status *</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                  required
                >
                  <option value="DISPONIVEL">Disponível</option>
                  <option value="EM_ATENDIMENTO">Em Atendimento</option>
                  <option value="EM_FOLGA">Em Folga</option>
                  <option value="INATIVO">Inativo</option>
                </select>
              </div>
              <div className="form-group">
                <label>Contato *</label>
                <input
                  type="tel"
                  value={formData.contato}
                  onChange={(e) => setFormData({ ...formData, contato: e.target.value })}
                  required
                  placeholder="(11) 99999-1111"
                />
              </div>
              {editingId && (
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={formData.ativo}
                      onChange={(e) => setFormData({ ...formData, ativo: e.target.checked })}
                    />
                    {' '}Ativo
                  </label>
                </div>
              )}
              {error && <div className="form-error">{error}</div>}
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  {editingId ? 'Salvar' : 'Criar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default GerenciarFuncionarios;
