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
      const error = await response.text();
      throw new Error(error || 'Erro ao registrar ocorrência');
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
      const error = await response.text();
      throw new Error(error || 'Erro ao despachar ocorrência');
    }
    return response.json();
  },

  async buscarPorId(id) {
    const response = await fetch(`${API_URL}/ocorrencias/${id}`);
    if (!response.ok) throw new Error('Erro ao buscar ocorrência');
    return response.json();
  },
};

