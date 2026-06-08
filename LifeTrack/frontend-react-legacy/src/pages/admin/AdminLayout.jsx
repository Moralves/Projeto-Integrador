import { useState } from 'react';
import GerenciarUsuarios from './sections/GerenciarUsuarios';
import GerenciarEquipes from './sections/GerenciarEquipes';
import GerenciarFuncionarios from './sections/GerenciarFuncionarios';
import GerenciarAmbulancias from './sections/GerenciarAmbulancias';
import AnaliseEstrategica from './sections/AnaliseEstrategica';
import Relatorios from './sections/Relatorios';
import './AdminLayout.css';

function AdminLayout({ onLogout }) {
  const [activeSection, setActiveSection] = useState('usuarios');

  const menuItems = [
    { id: 'usuarios', label: 'Usuários', icon: '👥' },
    { id: 'equipes', label: 'Equipes', icon: '👨‍👩‍👧‍👦' },
    { id: 'funcionarios', label: 'Funcionários', icon: '👤' },
    { id: 'ambulancias', label: 'Ambulâncias', icon: '🚑' },
    { id: 'analise', label: 'Análise Estratégica', icon: '📊' },
    { id: 'relatorios', label: 'Relatórios', icon: '📈' },
  ];

  const handleLogout = () => {
    if (window.confirm('Deseja realmente sair?')) {
      if (onLogout) {
        onLogout();
      }
    }
  };

  const renderContent = () => {
    switch (activeSection) {
      case 'usuarios':
        return <GerenciarUsuarios />;
      case 'equipes':
        return <GerenciarEquipes />;
      case 'funcionarios':
        return <GerenciarFuncionarios />;
      case 'ambulancias':
        return <GerenciarAmbulancias />;
      case 'analise':
        return <AnaliseEstrategica />;
      case 'relatorios':
        return <Relatorios />;
      default:
        return <GerenciarUsuarios />;
    }
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="sidebar-header">
          <h2>Painel Admin</h2>
        </div>
        <nav className="sidebar-nav">
          {menuItems.map((item) => (
            <button
              key={item.id}
              className={`nav-item ${activeSection === item.id ? 'active' : ''}`}
              onClick={() => setActiveSection(item.id)}
            >
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-label">{item.label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button className="nav-item logout-btn" onClick={handleLogout}>
            <span className="nav-icon">🚪</span>
            <span className="nav-label">Sair</span>
          </button>
        </div>
      </aside>
      <main className="admin-content">
        {renderContent()}
      </main>
    </div>
  );
}

export default AdminLayout;

