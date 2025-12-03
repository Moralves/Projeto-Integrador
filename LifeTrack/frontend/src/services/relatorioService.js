const API_URL = 'http://localhost:8081/api';

export const relatorioService = {
  async relatorioOcorrencias() {
    const response = await fetch(`${API_URL}/relatorios/ocorrencias`);
    if (!response.ok) throw new Error('Erro ao buscar relatório de ocorrências');
    return response.json();
  },
};

