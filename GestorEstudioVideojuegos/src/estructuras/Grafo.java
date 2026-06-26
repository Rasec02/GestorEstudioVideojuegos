package estructuras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Grafo {
    private Map<Integer, List<Arista>> adyacencia;

    public Grafo() {
        this.adyacencia = new LinkedHashMap<>();
    }

    public void agregarVertice(int codigo) {
        adyacencia.putIfAbsent(codigo, new ArrayList<>());
    }

    public boolean existeVertice(int codigo) {
        return adyacencia.containsKey(codigo);
    }

    public boolean agregarArista(int origen, int destino, double peso) {
        if (!existeVertice(origen) || !existeVertice(destino) || origen == destino) {
            return false;
        }
        for (Arista arista : adyacencia.get(origen)) {
            if (arista.getDestino() == destino) {
                return false;
            }
        }
        adyacencia.get(origen).add(new Arista(destino, peso));
        adyacencia.get(destino).add(new Arista(origen, peso));
        return true;
    }

    public void eliminarVertice(int codigo) {
        adyacencia.remove(codigo);
        for (List<Arista> vecinos : adyacencia.values()) {
            vecinos.removeIf(arista -> arista.getDestino() == codigo);
        }
    }

    public List<Arista> obtenerVecinos(int codigo) {
        return adyacencia.getOrDefault(codigo, new ArrayList<>());
    }

    public int cantidadVertices() {
        return adyacencia.size();
    }

    public RutaMinima dijkstra(int origen, int destino) {
        RutaMinima resultado = new RutaMinima();
        if (!existeVertice(origen) || !existeVertice(destino)) {
            return resultado;
        }

        Map<Integer, Double> distancia = new HashMap<>();
        Map<Integer, Integer> previo = new HashMap<>();
        for (int vertice : adyacencia.keySet()) {
            distancia.put(vertice, Double.POSITIVE_INFINITY);
        }
        distancia.put(origen, 0.0);

        PriorityQueue<EntradaCola> cola = new PriorityQueue<>((a, b) -> Double.compare(a.distancia, b.distancia));
        cola.add(new EntradaCola(origen, 0.0));

        while (!cola.isEmpty()) {
            EntradaCola actual = cola.poll();
            int vertice = actual.vertice;
            if (actual.distancia > distancia.get(vertice)) {
                continue;
            }
            if (vertice == destino) {
                break;
            }
            for (Arista arista : adyacencia.get(vertice)) {
                double nuevaDistancia = distancia.get(vertice) + arista.getPeso();
                if (nuevaDistancia < distancia.get(arista.getDestino())) {
                    distancia.put(arista.getDestino(), nuevaDistancia);
                    previo.put(arista.getDestino(), vertice);
                    cola.add(new EntradaCola(arista.getDestino(), nuevaDistancia));
                }
            }
        }

        if (distancia.get(destino) == Double.POSITIVE_INFINITY) {
            return resultado;
        }

        LinkedList<Integer> camino = new LinkedList<>();
        Integer paso = destino;
        while (paso != null) {
            camino.addFirst(paso);
            paso = previo.get(paso);
        }
        resultado.setCamino(camino);
        resultado.setCostoTotal(distancia.get(destino));
        return resultado;
    }

    public List<Integer> recorridoBFS(int origen) {
        List<Integer> visitados = new ArrayList<>();
        if (!existeVertice(origen)) {
            return visitados;
        }
        Map<Integer, Boolean> marcado = new HashMap<>();
        Queue<Integer> cola = new LinkedList<>();
        cola.add(origen);
        marcado.put(origen, true);
        while (!cola.isEmpty()) {
            int actual = cola.poll();
            visitados.add(actual);
            for (Arista arista : adyacencia.get(actual)) {
                if (!marcado.containsKey(arista.getDestino())) {
                    marcado.put(arista.getDestino(), true);
                    cola.add(arista.getDestino());
                }
            }
        }
        return visitados;
    }

    private static class EntradaCola {
        int vertice;
        double distancia;

        EntradaCola(int vertice, double distancia) {
            this.vertice = vertice;
            this.distancia = distancia;
        }
    }
}
