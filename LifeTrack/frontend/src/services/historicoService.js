const API_URL = 'http://localhost:8081/api';

export const historicoService = {
  async buscarPorOcorrencia(ocorrenciaId) {
    const response = await fetch(`${API_URL}/historico-ocorrencias/ocorrencia/${ocorrenciaId}`);
    if (!response.ok) {
      throw new Error('Erro ao buscar histórico da ocorrência');
    }
    return response.json();
  },

  async buscarMeuHistorico() {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      throw new Error('Usuário não autenticado');
    }
    const user = JSON.parse(userStr);
    const headers = {
      'X-User-Id': user.id.toString(),
    };
    
    const response = await fetch(`${API_URL}/historico-ocorrencias/meu-historico`, {
      headers,
    });
    if (!response.ok) {
      throw new Error('Erro ao buscar meu histórico');
    }
    return response.json();
  },
};

