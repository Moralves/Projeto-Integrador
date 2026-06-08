const API_URL = 'http://localhost:8081/api';

export const usuarioService = {
  async listar() {
    try {
      const response = await fetch(`${API_URL}/usuarios`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao listar usuários');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async criarUsuario(usuario) {
    try {
      const response = await fetch(`${API_URL}/usuarios`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(usuario),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao criar usuário');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async atualizarUsuario(id, usuario) {
    try {
      const response = await fetch(`${API_URL}/usuarios/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(usuario),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao atualizar usuário');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async deletarUsuario(id) {
    try {
      const response = await fetch(`${API_URL}/usuarios/${id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao deletar usuário');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async toggleStatusUsuario(id) {
    try {
      const response = await fetch(`${API_URL}/usuarios/${id}/toggle-status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao alterar status do usuário');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },
};

