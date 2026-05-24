import NivelBadge from '../components/NivelBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertaPanel from '../components/AlertaPanel';
import TransaccionItem from '../components/TransaccionItem';
import { useBilleteras } from '../hooks/useBilleteras';
import { useTransacciones } from '../hooks/useTransacciones';
import { usePuntosRecompensas, useNivelRecompensas } from '../hooks/useRecompensas';
import { useEffect, useState } from 'react';
import { obtenerUsuarioPorId } from '../services/api';

const moneyFormatter = new Intl.NumberFormat('es-CO', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const dateFormatter = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' });

function sortByRecentDate(items) {
  return [...items].sort((left, right) => {
    const leftDate = left.fecha ? new Date(left.fecha).getTime() : 0;
    const rightDate = right.fecha ? new Date(right.fecha).getTime() : 0;
    return rightDate - leftDate;
  });
}

export default function Dashboard({ userId, onNavigate }) {
  const [usuario, setUsuario] = useState(null);
  const [usuarioError, setUsuarioError] = useState(null);

  // Query hooks
  const billeterasQuery = useBilleteras(userId);
  const puntosQuery = usePuntosRecompensas(userId);
  const nivelQuery = useNivelRecompensas(userId);

  // Load usuario info
  useEffect(() => {
    let active = true;
    async function loadUsuario() {
      try {
        const data = await obtenerUsuarioPorId(userId);
        if (active) setUsuario(data);
      } catch (err) {
        if (active) setUsuarioError(err.message);
      }
    }
    loadUsuario();
    return () => { active = false; };
  }, [userId]);

  // Get billeteras (normalize response)
  const billeteras = billeterasQuery.data?.billeteras || billeterasQuery.data || [];

  // Get first wallet transactions if available
  const firstWalletId = billeteras.length > 0 ? billeteras[0].id : null;
  const transaccionesQuery = useTransacciones(firstWalletId);
  const transaccionesData = transaccionesQuery.data?.transacciones || transaccionesQuery.data || [];
  const transacciones = sortByRecentDate(transaccionesData).slice(0, 5);

  // Get rewards info
  const puntosData = puntosQuery.data || {};
  const nivelData = nivelQuery.data || {};
  const puntosActuales = Number(nivelData.puntosActuales ?? puntosData.puntos ?? puntosData.puntosAcumulados ?? 0);
  const nivelActual = nivelData.nivel || puntosData.nivel || usuario?.nivel || 'Bronce';
  const umbralSiguiente = Number(nivelData.umbralSiguiente ?? 0);
  const progreso = umbralSiguiente > 0 ? Math.min(100, Math.round((puntosActuales / umbralSiguiente) * 100)) : 100;

  const isLoading = billeterasQuery.isLoading || puntosQuery.isLoading || nivelQuery.isLoading;
  const error = billeterasQuery.error || puntosQuery.error || nivelQuery.error || usuarioError;
  const totalSaldo = billeteras.reduce((sum, wallet) => sum + Number(wallet.saldo || 0), 0);

  return (
    <section className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
      {error && <div className="col-span-1 lg:col-span-3"><AlertaPanel type="error" title="No se pudo cargar el panel" message={error?.message || String(error)} /></div>}
      {isLoading && <div className="col-span-1 lg:col-span-3 flex justify-center py-10"><LoadingSpinner /></div>}

      {!isLoading && (
        <>
          <div className="col-span-1 lg:col-span-2 flex flex-col gap-6">
            <div className="card-panel">
              <div className="flex flex-wrap justify-between items-baseline gap-2 mb-5">
                <h2 className="text-xl font-semibold">{usuario?.nombre || 'Usuario activo'}</h2>
                <span className="text-xs uppercase tracking-wider text-textoSecundario">Resumen general</span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <article className="stat-card-highlight">
                  <div className="flex flex-col gap-2">
                    <span className="text-4xl font-bold leading-[1.2] text-textoPrincipal">$ {moneyFormatter.format(totalSaldo)}</span>
                    <span className="text-xs uppercase tracking-wider text-textoSecundario">Saldo total</span>
                  </div>
                </article>
                <article className="stat-card">
                  <div className="flex flex-col gap-2">
                    <span className="text-2xl font-bold text-textoPrincipal">{billeteras.length}</span>
                    <span className="text-xs uppercase tracking-wider text-textoSecundario">Billeteras activas</span>
                  </div>
                </article>
                <article className="stat-card">
                  <div className="flex flex-col gap-2">
                    <span className="text-2xl font-bold text-textoPrincipal">{puntosActuales}</span>
                    <span className="text-xs uppercase tracking-wider text-textoSecundario">Puntos acumulados</span>
                  </div>
                </article>
              </div>

              <div className="flex flex-wrap justify-between items-center gap-3 mb-4">
                <h2 className="text-lg font-semibold">Billeteras activas</h2>
                <button type="button" className="btn-ghost" onClick={() => onNavigate('billeteras')}>Ver todas</button>
              </div>

              <div className="grid grid-cols-4 gap-4 pb-3 mb-2 border-b border-borde text-xs uppercase tracking-wider text-textoSecundario font-semibold">
                <div className="col-span-1">Nombre</div>
                <div className="col-span-1">Tipo</div>
                <div className="col-span-1">Saldo</div>
                <div className="col-span-1 text-right">Acción</div>
              </div>

              <div className="flex flex-col">
                {billeteras.length ? (
                  billeteras.map((wallet) => (
                    <article key={wallet.id} className="grid grid-cols-4 gap-4 items-center py-4 border-b border-borde last:border-b-0 hover:bg-fondo transition-colors duration-150 rounded px-2 -mx-2">
                      <div className="font-medium text-textoPrincipal">{wallet.nombre}</div>
                      <div className="text-sm text-textoSecundario">{wallet.tipo}</div>
                      <div className="text-base font-bold text-textoPrincipal">$ {moneyFormatter.format(Number(wallet.saldo || 0))}</div>
                      <div className="text-right">
                        <button type="button" className="text-sm px-4 py-2 border border-borde rounded text-textoSecundario hover:bg-textoSecundario hover:text-superficie transition-colors duration-150 active:scale-98" onClick={() => onNavigate('billeteras')}>Ver</button>
                      </div>
                    </article>
                  ))
                ) : (
                  <div className="border border-dashed border-borde rounded-md p-5 bg-fondo flex flex-col gap-2">
                    <p className="text-sm text-textoSecundario">Aun no hay billeteras registradas para este usuario.</p>
                    <button type="button" className="text-sm font-medium text-acento hover:text-acentoHover transition-colors w-fit" onClick={() => onNavigate('billeteras')}>Crear la primera billetera</button>
                  </div>
                )}
              </div>
            </div>
          </div>

          <aside className="col-span-1 lg:col-span-1 flex flex-col gap-6">
            <div className="card-panel">
              <div className="flex justify-between items-baseline mb-4">
                <h2 className="text-lg font-semibold">Nivel del usuario</h2>
                <span className="text-xs uppercase tracking-wider text-textoSecundario">Fidelización</span>
              </div>
              <div className="mb-4">
                <NivelBadge nivel={nivelActual} puntos={puntosActuales} />
              </div>
              <div className="text-sm text-textoSecundario mb-3">
                {umbralSiguiente > 0
                  ? `${puntosActuales} de ${umbralSiguiente} puntos hacia el siguiente nivel`
                  : 'Nivel máximo alcanzado'}
              </div>
              <div className="w-full h-2 bg-borde rounded-full overflow-hidden">
                <div className="h-full bg-gradient-brand transition-all duration-500 ease-smooth rounded-full" style={{ width: `${progreso}%` }} />
              </div>
            </div>
          </aside>

          <div className="col-span-1 lg:col-span-3">
            <div className="card-panel">
              <div className="flex flex-wrap justify-between items-center gap-3 mb-5">
                <h2 className="text-lg font-semibold">Actividad reciente</h2>
                <span className="text-xs uppercase tracking-wider text-textoSecundario">Últimas 5</span>
              </div>

              {transaccionesQuery.isLoading ? (
                <div className="flex justify-center py-5"><LoadingSpinner /></div>
              ) : transacciones.length ? (
                <div className="flex flex-col">
                  <div className="grid grid-cols-5 gap-4 pb-3 mb-2 border-b border-borde text-xs uppercase tracking-wider text-textoSecundario font-semibold">
                    <div className="col-span-1">Tipo</div>
                    <div className="col-span-1">Monto</div>
                    <div className="col-span-1">Estado</div>
                    <div className="col-span-1">Fecha</div>
                    <div className="col-span-1 text-right">Acción</div>
                  </div>
                  <div className="flex flex-col">
                    {transacciones.map((transaccion) => (
                      <TransaccionItem key={transaccion.id} transaccion={transaccion} />
                    ))}
                  </div>
                </div>
              ) : (
                <div className="border border-dashed border-borde rounded-md p-5 bg-fondo flex flex-col gap-2">
                  <p className="text-sm text-textoSecundario">Sin movimientos recientes. Registra una operacion para ver actividad aqui.</p>
                  <button type="button" className="text-sm font-medium text-acento hover:text-acentoHover transition-colors w-fit" onClick={() => onNavigate('transacciones')}>Crear una transaccion</button>
                </div>
              )}
            </div>
          </div>

          <div className="col-span-1 lg:col-span-3">
            <div className="card-panel">
              <div className="flex flex-wrap justify-between items-center gap-3 mb-5">
                <h2 className="text-lg font-semibold">Perfil del usuario</h2>
                <span className="text-xs uppercase tracking-wider text-textoSecundario">Detalle de cuenta</span>
              </div>

              {usuario ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="flex flex-col gap-3">
                    <div>
                      <span className="text-xs uppercase tracking-wider text-textoSecundario">Nombre</span>
                      <div className="text-base font-semibold text-textoPrincipal">{usuario.nombre}</div>
                    </div>
                    <div>
                      <span className="text-xs uppercase tracking-wider text-textoSecundario">Email</span>
                      <div className="text-sm text-textoPrincipal break-all">{usuario.email || 'Sin email registrado'}</div>
                    </div>
                    <div>
                      <span className="text-xs uppercase tracking-wider text-textoSecundario">Telefono</span>
                      <div className="text-sm text-textoPrincipal">{usuario.telefono || 'Sin telefono registrado'}</div>
                    </div>
                  </div>

                  <div className="flex flex-col gap-3">
                    <div>
                      <span className="text-xs uppercase tracking-wider text-textoSecundario">Nivel</span>
                      <div className="mt-1 inline-flex items-center px-3 py-1 rounded-full border border-borde bg-fondo text-xs font-semibold text-textoPrincipal">
                        {nivelActual}
                      </div>
                    </div>
                    <div>
                      <span className="text-xs uppercase tracking-wider text-textoSecundario">Puntos acumulados</span>
                      <div className="text-lg font-semibold text-textoPrincipal">{puntosActuales}</div>
                    </div>
                    <div>
                      <span className="text-xs uppercase tracking-wider text-textoSecundario">Fecha de registro</span>
                      <div className="text-sm text-textoPrincipal">
                        {usuario.fechaRegistro ? dateFormatter.format(new Date(usuario.fechaRegistro)) : 'Sin fecha registrada'}
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="border border-dashed border-borde rounded-md p-5 bg-fondo">
                  <p className="text-sm text-textoSecundario">No hay informacion del usuario disponible.</p>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </section>
  );
}
