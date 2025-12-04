const API_URL = 'http://localhost:8081/api';

// Obter ID do usuário do localStorage
const getUserId = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    const user = JSON.parse(userStr);
    return user.id;
  }
  return null;
};

export const ocorrenciaService = {
  async listar() {
    const response = await fetch(`${API_URL}/ocorrencias`);
    if (!response.ok) throw new Error('Erro ao listar ocorrências');
    return response.json();
  },

  async registrar(dados) {
    const userId = getUserId();
    const headers = {
      'Content-Type': 'application/json',
    };
    
    if (userId) {
      headers['X-User-Id'] = userId.toString();
    }

    const response = await fetch(`${API_URL}/ocorrencias`, {
      method: 'POST',
      headers,
      body: JSON.stringify(dados),
    });
    
    if (!response.ok) {
      let errorMessage = 'Erro ao registrar ocorrência';
      try {
        // Tentar ler como JSON primeiro
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
          const errorData = await response.json();
          if (errorData.error) {
            errorMessage = errorData.error;
          } else if (errorData.message) {
            errorMessage = errorData.message;
          } else if (typeof errorData === 'string') {
            errorMessage = errorData;
          }
        } else {
          // Se não for JSON, ler como texto
          const errorText = await response.text();
          if (errorText) {
            errorMessage = errorText;
          }
        }
      } catch (err) {
        // Se falhar ao ler, usar mensagem padrão
        errorMessage = `Erro ${response.status}: ${response.statusText}`;
      }
      throw new Error(errorMessage);
    }
    return response.json();
  },

  async despachar(idOcorrencia) {
    const userId = getUserId();
    const headers = {
      'Content-Type': 'application/json',
    };
    
    if (userId) {
      headers['X-User-Id'] = userId.toString();
    }

    const response = await fetch(`${API_URL}/ocorrencias/${idOcorrencia}/despachar`, {
      method: 'POST',
      headers,
    });
    
    if (!response.ok) {
      let errorMessage = 'Erro ao despachar ocorrência';
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
    return response.json();
  },

  async buscarPorId(id) {
    const response = await fetch(`${API_URL}/ocorrencias/${id}`);
    if (!response.ok) throw new Error('Erro ao buscar ocorrência');
    return response.json();
  },

  async sugerirAmbulancias(idOcorrencia) {
    const response = await fetch(`${API_URL}/ocorrencias/${idOcorrencia}/ambulancias-sugeridas`);
    if (!response.ok) {
      let errorMessage = 'Erro ao buscar ambulâncias sugeridas';
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
    return response.json();
  },
};

