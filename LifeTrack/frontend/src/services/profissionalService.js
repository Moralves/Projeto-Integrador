const API_URL = 'http://localhost:8081/api';

export const profissionalService = {
  async listar(turno = null, status = null) {
    try {
      let url = `${API_URL}/profissionais`;
      const params = new URLSearchParams();
      if (turno) params.append('turno', turno);
      if (status) params.append('status', status);
      if (params.toString()) {
        url += '?' + params.toString();
      }

      const response = await fetch(url, {
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

  async listarDisponiveis(turno = null) {
    try {
      let url = `${API_URL}/profissionais/disponiveis`;
      if (turno) {
        url += '?turno=' + turno;
      }

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao listar profissionais disponíveis');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async buscarPorId(id) {
    try {
      const response = await fetch(`${API_URL}/profissionais/${id}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao buscar profissional');
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

  async editar(id, dados) {
    try {
      const response = await fetch(`${API_URL}/profissionais/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dados),
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao editar profissional';
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

  async alterarStatus(id, status) {
    try {
      const response = await fetch(`${API_URL}/profissionais/${id}/status?status=${status}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao alterar status';
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

  async desativar(id) {
    try {
      const response = await fetch(`${API_URL}/profissionais/${id}/desativar`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao desativar profissional';
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
};

