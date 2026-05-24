import { useState } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import Billeteras from './pages/Billeteras';
import Transacciones from './pages/Transacciones';
import Analitica from './pages/Analitica';
import Recompensas from './pages/Recompensas';
import Notificaciones from './pages/Notificaciones';
import OperacionesProgramadas from './pages/OperacionesProgramadas';
import Usuarios from './pages/Usuarios';
import { useAuth } from './auth/AuthContext';

const VIEW_TITLES = {
  dashboard: 'Dashboard',
  billeteras: 'Billeteras',
  transacciones: 'Transacciones',
  analitica: 'Analítica',
  recompensas: 'Recompensas',
  usuarios: 'Usuarios',
  notificaciones: 'Notificaciones',
  programadas: 'Operaciones Programadas',
};

const VIEW_DESCRIPTIONS = {
  dashboard: 'Resumen de saldos, billeteras y actividad reciente.',
  billeteras: 'Administra tus billeteras y consulta saldos.',
  transacciones: 'Registra depósitos, retiros y transferencias.',
  analitica: 'Visualiza patrones y métricas de uso.',
  recompensas: 'Consulta puntos, niveles y beneficios.',
  usuarios: 'Gestiona perfiles y datos de cuenta.',
  notificaciones: 'Revisa alertas y avisos del sistema.',
  programadas: 'Programa operaciones automáticas.',
};

export default function App() {
  const { user, logout } = useAuth();
  const [activeView, setActiveView] = useState('dashboard');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const activeUserId = user?.id || 1;

  const sharedProps = {
    userId: activeUserId,
    onNavigate: setActiveView,
  };

  function renderView() {
    switch (activeView) {
      case 'billeteras': return <Billeteras {...sharedProps} />;
      case 'transacciones': return <Transacciones {...sharedProps} />;
      case 'analitica': return <Analitica {...sharedProps} />;
      case 'recompensas': return <Recompensas {...sharedProps} />;
      case 'usuarios': return <Usuarios {...sharedProps} />;
      case 'notificaciones': return <Notificaciones {...sharedProps} />;
      case 'programadas': return <OperacionesProgramadas {...sharedProps} />;
      case 'dashboard': default: return <Dashboard {...sharedProps} />;
    }
  }

  return (
    <div className="min-h-screen text-textoPrincipal font-sans flex animate-pageEnter">
      <Sidebar activeView={activeView} onNavigate={setActiveView} collapsed={sidebarCollapsed} setCollapsed={setSidebarCollapsed} />
      <main className="flex-1 min-w-0">
        <div className="max-w-[1200px] mx-auto w-full px-5 py-6 lg:px-8">
          <header className="flex flex-col lg:flex-row lg:justify-between lg:items-start gap-4 mb-6">
            <div>
              <p className="text-xs uppercase tracking-wider text-acento font-semibold">Plataforma fintech</p>
              <h1 className="page-title mt-1">{VIEW_TITLES[activeView] || 'Dashboard'}</h1>
              <p className="text-sm text-textoSecundario mt-2 max-w-xl">
                {VIEW_DESCRIPTIONS[activeView] || VIEW_DESCRIPTIONS.dashboard}
              </p>
            </div>
            <div className="flex flex-row lg:flex-col items-center lg:items-start gap-3 p-4 border border-borde bg-superficie/90 backdrop-blur rounded-lg shadow-media transition-all duration-200 hover:shadow-flotante hover:border-bordeActivo">
              <span className="text-xs uppercase tracking-wider text-textoSecundario">Sesión activa</span>
              <strong className="text-xl font-bold truncate max-w-[200px]">{user?.nombre || user?.email || 'Usuario'}</strong>
              <button type="button" onClick={() => logout('logout')} className="btn-ghost text-xs">Cerrar sesión</button>
            </div>
          </header>
          <div className="animate-pageEnter" key={activeView}>{renderView()}</div>
        </div>
      </main>
    </div>
  );
}