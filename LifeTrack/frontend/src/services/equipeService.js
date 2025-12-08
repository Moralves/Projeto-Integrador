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
        let errorMessage = 'Erro ao criar equipe';
        try {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            errorMessage = errorData.error || errorData.message || errorMessage;
          } else {
            const errorText = await response.text();
            if (errorText) {
              errorMessage = errorText;
            }
          }
        } catch (err) {
          errorMessage = `Erro ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async atualizarEquipe(id, dados) {
    try {
      const response = await fetch(`${API_URL}/equipes/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dados),
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao atualizar equipe';
        try {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            errorMessage = errorData.error || errorData.message || errorMessage;
          } else {
            const errorText = await response.text();
            if (errorText) {
              errorMessage = errorText;
            }
          }
        } catch (err) {
          errorMessage = `Erro ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async verificarStatus(id) {
    try {
      const response = await fetch(`${API_URL}/equipes/${id}/status`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao verificar status da equipe');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async removerEquipePorAmbulancia(idAmbulancia) {
    try {
      const response = await fetch(`${API_URL}/equipes/por-ambulancia/${idAmbulancia}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao remover equipe';
        try {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            errorMessage = errorData.error || errorData.message || errorMessage;
          } else {
            const errorText = await response.text();
            if (errorText) {
              errorMessage = errorText;
            }
          }
        } catch (err) {
          errorMessage = `Erro ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      // Resposta vazia (204 No Content) - não tentar fazer parse JSON
      return null;
    } catch (error) {
      throw error;
    }
  },
};

