const API_URL = 'http://localhost:8080/api';

export const authService = {
  async login(username, password) {
    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Erro ao fazer login');
      }

      const data = await response.json();
      
      // Salvar token no localStorage
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify({
        id: data.id,
        username: data.username,
        nome: data.nome,
        email: data.email,
        roles: data.roles || [],
      }));

      return data;
    } catch (error) {
      throw error;
    }
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getToken() {
    return localStorage.getItem('token');
  },

  getUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  getAuthHeaders() {
    const token = this.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  },
};

