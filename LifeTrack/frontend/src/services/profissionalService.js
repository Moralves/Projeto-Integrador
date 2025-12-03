const API_URL = 'http://localhost:8081/api';

export const profissionalService = {
  async listar() {
    try {
      const response = await fetch(`${API_URL}/profissionais`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao listar profissionais');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async cadastrar(profissional) {
    try {
      const response = await fetch(`${API_URL}/profissionais`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(profissional),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao cadastrar profissional');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async desativar(id) {
    try {
      const response = await fetch(`${API_URL}/profissionais/${id}/desativar`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao desativar profissional');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },
};

