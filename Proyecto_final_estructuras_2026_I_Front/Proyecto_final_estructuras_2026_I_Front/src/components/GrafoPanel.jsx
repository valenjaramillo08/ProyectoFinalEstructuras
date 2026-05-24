import { useRef, useEffect, useState, useMemo } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import { buildGraphData } from '../utils/grafoUtils';

const GRAPH_PROPS = {
  nodeLabel: 'name',
  nodeAutoColorBy: 'id',
  nodeColor: () => '#d38343',
  linkColor: () => 'rgba(180, 180, 180, 0.5)',
  linkWidth: 2,
  linkDirectionalArrowLength: 3.5,
  linkDirectionalArrowRelPos: 1,
  backgroundColor: 'transparent',
};

export default function GrafoPanel({ grafo, descripcion, isLoading }) {
  const containerRef = useRef(null);
  const [graphDimensions, setGraphDimensions] = useState({ width: 600, height: 350 });

  useEffect(() => {
    if (!containerRef.current) return;
    const observer = new ResizeObserver((entries) => {
      if (entries[0]) {
        setGraphDimensions({
          width: entries[0].contentRect.width,
          height: 350,
        });
      }
    });
    observer.observe(containerRef.current);
    return () => observer.disconnect();
  }, []);

  const graphData = useMemo(
    () => buildGraphData(grafo),
    [grafo.nodos, grafo.enlaces],
  );

  return (
    <div className="border border-borde rounded-md overflow-hidden bg-superficie flex flex-col" ref={containerRef}>
      {descripcion && (
        <p className="text-xs text-textoSecundario px-4 pt-3">{descripcion}</p>
      )}
      <div className="flex justify-end px-4 pt-2">
        <span className="text-xs uppercase tracking-wider text-textoSecundario">
          Nodos {grafo.nodos?.length || 0} | Aristas {graphData.links.length}
        </span>
      </div>
      {isLoading ? (
        <p className="text-sm text-textoSecundario p-5">Cargando grafo...</p>
      ) : grafo.nodos?.length ? (
        <div className="flex justify-center pb-2">
          <ForceGraph2D
            width={graphDimensions.width}
            height={graphDimensions.height}
            graphData={graphData}
            {...GRAPH_PROPS}
          />
        </div>
      ) : (
        <p className="text-sm text-textoSecundario p-5">Sin datos de grafo disponibles para esta categoría.</p>
      )}
    </div>
  );
}
