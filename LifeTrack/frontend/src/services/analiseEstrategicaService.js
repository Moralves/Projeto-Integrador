const API_URL = 'http://localhost:8081/api';

export const analiseEstrategicaService = {
  async obterBairrosSugeridos() {
    const response = await fetch(`${API_URL}/bairros/sugeridos`);
    if (!response.ok) {
      throw new Error('Erro ao obter bairros sugeridos');
    }
    return response.json();
  },
};

