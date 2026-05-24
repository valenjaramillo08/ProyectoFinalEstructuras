export const GRAFOS_OPCIONES = [
  {
    id: 'transferencias',
    label: 'Transferencias entre usuarios',
    descripcion: 'Representa transferencias entre usuarios del sistema.',
  },
  {
    id: 'billeteras',
    label: 'Relaciones entre billeteras',
    descripcion: 'Analiza relaciones entre billeteras según movimientos registrados.',
  },
  {
    id: 'rutas',
    label: 'Rutas frecuentes de dinero',
    descripcion: 'Detecta rutas frecuentes de movimiento de dinero entre usuarios.',
  },
  {
    id: 'patrones',
    label: 'Patrones de interacción financiera',
    descripcion: 'Estudia patrones de interacción financiera bidireccional.',
  },
];

export function ensureArray(data, keys = []) {
  if (Array.isArray(data)) return data;
  if (data && typeof data === 'object') {
    for (const key of keys) {
      if (Array.isArray(data[key])) return data[key];
    }
  }
  return [];
}

export function parseGrafoResponse(grafoData) {
  const payload = grafoData || {};
  return {
    tipo: payload.tipo || 'transferencias',
    titulo: payload.titulo || 'Grafo analítico',
    nodos: Array.isArray(payload) ? payload : ensureArray(payload, ['nodos', 'nodes', 'vertices']),
    enlaces: ensureArray(payload, ['enlaces', 'links', 'edges', 'aristas', 'relaciones', 'conexiones']),
  };
}

export function buildGraphData(grafo) {
  if (!grafo.nodos?.length) return { nodes: [], links: [] };

  const nodes = grafo.nodos.map((n, i) => {
    const id = String(n.id ?? n.nombre ?? n.label ?? i);
    return {
      ...n,
      id,
      name: String(n.label || n.nombre || n.id || `Nodo ${i}`),
    };
  });

  const adjacencyLinks = [];
  grafo.nodos.forEach((n, i) => {
    const sourceId = String(n.id ?? n.nombre ?? n.label ?? i);
    const neighbors = ensureArray(n, ['vecinos', 'adyacentes', 'enlaces', 'links', 'edges', 'aristas', 'conexiones', 'relaciones']);
    neighbors.forEach((neighbor) => {
      const targetId = typeof neighbor === 'object'
        ? String(neighbor.id ?? neighbor.nombre ?? neighbor.label ?? neighbor.destino ?? neighbor.target ?? neighbor.to ?? '')
        : String(neighbor);

      if (targetId) {
        adjacencyLinks.push({
          source: sourceId,
          target: targetId,
          ...(typeof neighbor === 'object' ? neighbor : {}),
        });
      }
    });
  });

  const directLinks = (grafo.enlaces || []).map((l) => {
    const s = l.source?.id ?? l.source?.nombre ?? l.source ?? l.origen?.id ?? l.origen?.nombre ?? l.origen ?? l.fuente ?? l.from;
    const t = l.target?.id ?? l.target?.nombre ?? l.target ?? l.destino?.id ?? l.destino?.nombre ?? l.destino ?? l.to;
    return {
      ...l,
      source: String(s),
      target: String(t),
    };
  });

  const links = [...directLinks, ...adjacencyLinks].filter(
    (l) => l.source && l.target && l.source !== 'undefined' && l.target !== 'undefined',
  );

  return { nodes, links };
}
