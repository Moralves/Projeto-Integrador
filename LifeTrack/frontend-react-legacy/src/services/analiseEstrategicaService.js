const API_URL = 'http://localhost:8081/api';

export const analiseEstrategicaService = {
  async obterBairrosSugeridos(tipoAmbulancia = null) {
    let url = `${API_URL}/bairros/sugeridos`;
    if (tipoAmbulancia) {
      url += `?tipoAmbulancia=${tipoAmbulancia}`;
    }
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error('Erro ao obter bairros sugeridos');
    }
    return response.json();
  },
};



