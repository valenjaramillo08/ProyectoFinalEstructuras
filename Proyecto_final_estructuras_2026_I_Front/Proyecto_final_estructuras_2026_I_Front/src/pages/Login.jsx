import { useState } from 'react';
import { LoaderCircle, ShieldCheck } from 'lucide-react';
import AlertaPanel from '../components/AlertaPanel';
import { useAuth } from '../auth/AuthContext';

function isValidPhone(phone) {
  return /^[0-9+\s-]{7,15}$/.test(String(phone || '').trim());
}

export default function Login({ sessionExpired = false, initialError = '' }) {
  const { login, register, isLoading } = useAuth();
  const [isRegistering, setIsRegistering] = useState(false);
  const [nombre, setNombre] = useState('');
  const [telefono, setTelefono] = useState('');
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(initialError || '');

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    if (!identifier.trim() || !password.trim()) {
      setError('Completa email y contraseña para continuar.');
      return;
    }

    if (isRegistering && !nombre.trim()) {
      setError('El nombre es obligatorio para crear una cuenta.');
      return;
    }

    if (isRegistering && !telefono.trim()) {
      setError('El teléfono es obligatorio para crear una cuenta.');
      return;
    }

    if (isRegistering && !isValidPhone(telefono)) {
      setError('El teléfono no tiene un formato válido (7 a 15 dígitos).');
      return;
    }

    try {
      if (isRegistering) {
        await register({
          nombre: nombre.trim(),
          email: identifier.trim(),
          password,
          telefono: telefono.trim(),
        });
      } else {
        await login({ email: identifier.trim(), password });
      }
    } catch (requestError) {
      setError(requestError?.message || (isRegistering ? 'No fue posible crear la cuenta.' : 'No fue posible iniciar sesión.'));
    }
  }

  return (
    <main className="min-h-screen bg-gradient-app flex items-center justify-center p-6">
      <section className="w-full max-w-md bg-superficie/95 backdrop-blur border border-borde rounded-xl shadow-flotante p-8">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-11 h-11 rounded-full bg-gradient-brand text-white flex items-center justify-center shadow-acento">
            <ShieldCheck size={22} />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-textoPrincipal">Fintech Wallet</h1>
            <p className="text-sm text-textoSecundario">{isRegistering ? 'Crea una cuenta nueva' : 'Acceso seguro a tu cuenta'}</p>
          </div>
        </div>

        {sessionExpired && <AlertaPanel type="error" title="Sesión expirada" message="Tu sesión caducó. Vuelve a iniciar sesión." />}
        {error && <div className="mt-4"><AlertaPanel type="error" title={isRegistering ? "Error de registro" : "Error de autenticación"} message={error} /></div>}

        <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4" noValidate>
          {isRegistering && (
            <>
              <div className="flex flex-col gap-2 animate-pageEnter">
                <label className="text-sm font-medium text-textoSecundario">Nombre completo</label>
                <input
                  type="text"
                  value={nombre}
                  onChange={(event) => setNombre(event.target.value)}
                  placeholder="Ej. Juan Pérez"
                  className="input-field"
                  autoComplete="name"
                />
              </div>
              <div className="flex flex-col gap-2 animate-pageEnter">
                <label className="text-sm font-medium text-textoSecundario">Teléfono</label>
                <input
                  type="tel"
                  value={telefono}
                  onChange={(event) => setTelefono(event.target.value)}
                  placeholder="Ej. 3001234567"
                  className="input-field"
                  autoComplete="tel"
                />
              </div>
            </>
          )}

          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium text-textoSecundario">Email</label>
            <input
              type="email"
              value={identifier}
              onChange={(event) => setIdentifier(event.target.value)}
              placeholder="correo@dominio.com"
              className="input-field"
              autoComplete="email"
            />
          </div>

          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium text-textoSecundario">Contraseña</label>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="••••••••"
              className="input-field"
              autoComplete={isRegistering ? "new-password" : "current-password"}
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="btn-primary w-full mt-2"
          >
            {isLoading ? <LoaderCircle className="animate-spin" size={18} /> : null}
            {isLoading ? 'Procesando...' : (isRegistering ? 'Crear cuenta' : 'Iniciar sesión')}
          </button>
        </form>

        <div className="mt-6 pt-6 border-t border-borde text-center">
          <p className="text-sm text-textoSecundario">
            {isRegistering ? '¿Ya tienes una cuenta?' : '¿No tienes una cuenta?'}
            <button
              type="button"
              onClick={() => {
                setIsRegistering(!isRegistering);
                setError('');
                setTelefono('');
              }}
              className="ml-2 btn-ghost"
            >
              {isRegistering ? 'Iniciar sesión' : 'Regístrate aquí'}
            </button>
          </p>
        </div>
      </section>
    </main>
  );
}
