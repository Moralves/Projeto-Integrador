import { useState, useEffect } from 'react';
import Login from './components/auth/Login';
import AdminDashboard from './pages/admin/AdminDashboard';
import UserDashboard from './pages/user/UserDashboard';
import { authService } from './services/authService';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Modo desenvolvimento: permitir acessar diretamente a tela de admin sem backend
    // URL: http://localhost:5173/admin-dev
    if (window.location.pathname === '/admin-dev') {
      const mockAdmin = {
        id: 0,
        username: 'admin-dev',
        nome: 'Administrador (Dev)',
        email: 'admin-dev@lifetrack.com',
        roles: ['ADMIN'],
      };
      setUser(mockAdmin);
      setIsAuthenticated(true);
      return;
    }

    // Verificar se já existe um token salvo
    if (authService.isAuthenticated()) {
      const savedUser = authService.getUser();
      setUser(savedUser);
      setIsAuthenticated(true);
    }
  }, []);

  const handleLoginSuccess = (loginData) => {
    setUser(loginData);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  const isAdmin = () => {
    if (!user || !user.roles) return false;
    return Array.from(user.roles).some(role => role === 'ADMIN' || role === 'ROLE_ADMIN');
  };

  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>LifeTrack - Sistema de Atendimento</h1>
        <div className="user-info">
          <span>Olá, {user?.nome || user?.username}</span>
          {isAdmin() && <span className="admin-badge">Admin</span>}
          <button onClick={handleLogout} className="logout-button">
            Sair
          </button>
        </div>
      </header>
      <main className="app-main">
        {isAdmin() ? (
          <AdminDashboard />
        ) : (
          <UserDashboard />
        )}
      </main>
    </div>
  );
}

export default App;
