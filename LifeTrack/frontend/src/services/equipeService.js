const API_URL = 'http://localhost:8081/api';

export const equipeService = {
  async listarEquipes() {
    try {
      const response = await fetch(`${API_URL}/equipes`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao listar equipes');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async listarEquipesDisponiveis() {
    try {
      const response = await fetch(`${API_URL}/equipes/disponiveis`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao listar equipes disponíveis');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async criarEquipe(dados) {
    try {
      const response = await fetch(`${API_URL}/equipes`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dados),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao criar equipe');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },
};

