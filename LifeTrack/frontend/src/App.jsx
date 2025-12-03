import { useState, useEffect } from 'react';
import AdminLayout from './pages/admin/AdminLayout';
import OperatorLayout from './pages/operador/OperatorLayout';
import Login from './pages/Login';
import { authService } from './services/authService';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Verificar se já está autenticado
    if (authService.isAuthenticated()) {
      setIsAuthenticated(true);
      setUser(authService.getCurrentUser());
    }
    setLoading(false);
  }, []);

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
    setUser(authService.getCurrentUser());
  };

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUser(null);
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '100vh' 
      }}>
        <div>Carregando...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  // Roteamento baseado no perfil do usuário
  if (user && user.perfil === 'ADMIN') {
    return <AdminLayout onLogout={handleLogout} />;
  } else if (user && user.perfil === 'USER') {
    return <OperatorLayout onLogout={handleLogout} />;
  }

  // Fallback: se não tiver perfil definido, mostra login
  return <Login onLoginSuccess={handleLoginSuccess} />;
}

export default App;
