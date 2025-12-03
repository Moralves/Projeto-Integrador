const API_URL = 'http://localhost:8081/api';

export const bairroService = {
  async listar() {
    const response = await fetch(`${API_URL}/bairros`);
    if (!response.ok) throw new Error('Erro ao listar bairros');
    return response.json();
  },
};

