import { useState } from 'react';
import RegistrarOcorrencia from './sections/RegistrarOcorrencia';
import ListarOcorrencias from './sections/ListarOcorrencias';
import './OperatorLayout.css';

function OperatorLayout({ onLogout }) {
  const [activeSection, setActiveSection] = useState('registrar');

  const menuItems = [
    { id: 'registrar', label: 'Registrar Ocorrência', icon: '📝' },
    { id: 'ocorrencias', label: 'Ocorrências', icon: '📋' },
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
      case 'registrar':
        return <RegistrarOcorrencia onRegistroSuccess={() => setActiveSection('ocorrencias')} />;
      case 'ocorrencias':
        return <ListarOcorrencias />;
      default:
        return <RegistrarOcorrencia onRegistroSuccess={() => setActiveSection('ocorrencias')} />;
    }
  };

  return (
    <div className="operator-layout">
      <aside className="operator-sidebar">
        <div className="sidebar-header">
          <h2>Painel Operador</h2>
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
            <span className="nav-label">Sair</span>
          </button>
        </div>
      </aside>
      <main className="operator-content">
        {renderContent()}
      </main>
    </div>
  );
}

export default OperatorLayout;

