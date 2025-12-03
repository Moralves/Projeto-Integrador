const API_URL = 'http://localhost:8081/api';

export const authService = {
  async login(login, senha) {
    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ login, senha }),
      });

      if (!response.ok) {
        let errorMessage = 'Erro ao fazer login';
        try {
          const errorText = await response.text();
          errorMessage = errorText || errorMessage;
        } catch (e) {
          // Se não conseguir ler o texto, usar mensagem padrão
        }
        throw new Error(errorMessage);
      }

      const data = await response.json();
      
      // Salvar dados do usuário no localStorage
      localStorage.setItem('user', JSON.stringify(data));
      localStorage.setItem('token', data.token || 'mock-token');
      
      return data;
    } catch (error) {
      throw error;
    }
  },

  logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return localStorage.getItem('user') !== null;
  },

  isAdmin() {
    const user = this.getCurrentUser();
    return user && (user.perfil === 'ADMIN' || user.perfil?.includes('ADMIN'));
  }
};

