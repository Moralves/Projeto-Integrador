const API_URL = 'http://localhost:8081/api';

export const ambulanciaService = {
  async listar() {
    try {
      const response = await fetch(`${API_URL}/ambulancias`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao listar ambulâncias');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async cadastrar(ambulancia) {
    try {
      const response = await fetch(`${API_URL}/ambulancias`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(ambulancia),
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao cadastrar ambulância';
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

  async atualizar(id, dados) {
    try {
      const response = await fetch(`${API_URL}/ambulancias/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dados),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao atualizar ambulância');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async ativar(id) {
    try {
      const response = await fetch(`${API_URL}/ambulancias/${id}/ativar`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao ativar ambulância');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async desativar(id) {
    try {
      const response = await fetch(`${API_URL}/ambulancias/${id}/desativar`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao desativar ambulância');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },

  async verificarEmAtendimento(id) {
    try {
      const response = await fetch(`${API_URL}/ambulancias/${id}/em-atendimento`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Erro ao verificar status de atendimento');
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },
};

