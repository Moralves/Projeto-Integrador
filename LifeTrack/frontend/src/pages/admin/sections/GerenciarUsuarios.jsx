import { useState, useEffect } from 'react';
import { usuarioService } from '../../../services/usuarioService';
import '../AdminDashboard.css';

function GerenciarUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingUsuario, setEditingUsuario] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    nome: '',
    email: '',
    telefone: '',
  });

  useEffect(() => {
    carregarUsuarios();
  }, []);

  const carregarUsuarios = async () => {
    try {
      setLoading(true);
      const data = await usuarioService.listar();
      setUsuarios(data);
      setError('');
    } catch (err) {
      setError('Erro ao carregar usuários: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (usuario = null) => {
    if (usuario) {
      setEditingUsuario(usuario);
      setFormData({
        username: usuario.username,
        password: '',
        nome: usuario.nome,
        email: usuario.email,
        telefone: usuario.telefone || '',
      });
    } else {
      setEditingUsuario(null);
      setFormData({
        username: '',
        password: '',
        nome: '',
        email: '',
        telefone: '',
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingUsuario(null);
    setFormData({
      username: '',
      password: '',
      nome: '',
      email: '',
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      
      if (editingUsuario) {
        await usuarioService.atualizarUsuario(editingUsuario.id, formData);
      } else {
        if (!formData.password) {
          setError('Senha é obrigatória para novos usuários');
          return;
        }
        await usuarioService.criarUsuario(formData);
      }
      
      handleCloseModal();
      carregarUsuarios();
    } catch (err) {
      setError('Erro ao salvar usuário: ' + err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Tem certeza que deseja deletar este usuário?')) {
      return;
    }

    try {
      await usuarioService.deletarUsuario(id);
      carregarUsuarios();
    } catch (err) {
      setError('Erro ao deletar usuário: ' + err.message);
    }
  };

  const handleToggleStatus = async (id) => {
    try {
      await usuarioService.toggleStatusUsuario(id);
      carregarUsuarios();
    } catch (err) {
      setError('Erro ao alterar status: ' + err.message);
    }
  };


  return (
    <div className="admin-dashboard">
      <div className="dashboard-header">
        <h1>Gerenciamento de Usuários</h1>
        <button className="btn-primary" onClick={() => handleOpenModal()}>
          + Novo Usuário
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Carregando usuários...</div>
      ) : (
        <div className="usuarios-table-container">
          <table className="usuarios-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuário</th>
                <th>Nome</th>
                <th>Email</th>
                <th>Telefone</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.length === 0 ? (
                <tr>
                  <td colSpan="8" className="empty-message">
                    Nenhum usuário cadastrado
                  </td>
                </tr>
              ) : (
                usuarios.map((usuario) => (
                  <tr key={usuario.id}>
                    <td>{usuario.id}</td>
                    <td>{usuario.username}</td>
                    <td>{usuario.nome}</td>
                    <td>{usuario.email}</td>
                    <td>{usuario.telefone || '-'}</td>
                    <td>
                      <span className="role-badge">
                        {usuario.roles && usuario.roles.length > 0 
                          ? Array.from(usuario.roles).join(', ') 
                          : 'USER'}
                      </span>
                    </td>
                    <td>
                      <span className={`status-badge ${usuario.ativo ? 'ativo' : 'inativo'}`}>
                        {usuario.ativo ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td className="actions">
                      <button
                        className="btn-edit"
                        onClick={() => handleOpenModal(usuario)}
                      >
                        Editar
                      </button>
                      <button
                        className={`btn-toggle ${usuario.ativo ? 'desativar' : 'ativar'}`}
                        onClick={() => handleToggleStatus(usuario.id)}
                      >
                        {usuario.ativo ? 'Desativar' : 'Ativar'}
                      </button>
                      <button
                        className="btn-delete"
                        onClick={() => handleDelete(usuario.id)}
                      >
                        Deletar
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
              <h2>{editingUsuario ? 'Editar Usuário' : 'Novo Usuário'}</h2>
              <button className="btn-close" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Usuário *</label>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Senha {editingUsuario ? '(deixe em branco para manter)' : '*'}</label>
                <input
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  required={!editingUsuario}
                />
              </div>
              <div className="form-group">
                <label>Nome Completo *</label>
                <input
                  type="text"
                  value={formData.nome}
                  onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Telefone *</label>
                <input
                  type="tel"
                  value={formData.telefone}
                  onChange={(e) => setFormData({ ...formData, telefone: e.target.value })}
                  required
                  placeholder="(00) 00000-0000"
                />
              </div>
              {error && <div className="form-error">{error}</div>}
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={handleCloseModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  {editingUsuario ? 'Atualizar' : 'Criar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default GerenciarUsuarios;

