package aplicacion;

import estructuras.ArbolBinarioBusqueda;
import estructuras.Arista;
import estructuras.Grafo;
import estructuras.RutaMinima;
import modelo.Videojuego;
import java.util.List;

public class Catalogo {
    private ArbolBinarioBusqueda arbol;
    private Grafo grafo;

    public Catalogo() {
        this.arbol = new ArbolBinarioBusqueda();
        this.grafo = new Grafo();
    }

    public boolean registrarVideojuego(Videojuego videojuego) {
        boolean insertado = arbol.insertar(videojuego);
        if (insertado) {
            grafo.agregarVertice(videojuego.getCodigo());
        }
        return insertado;
    }

    public Videojuego buscarVideojuego(int codigo) {
        return arbol.buscar(codigo);
    }

    public boolean modificarVideojuego(int codigo, String titulo, String genero, int anio, double calificacion) {
        return arbol.modificar(codigo, titulo, genero, anio, calificacion);
    }

    public boolean eliminarVideojuego(int codigo) {
        boolean eliminado = arbol.eliminar(codigo);
        if (eliminado) {
            grafo.eliminarVertice(codigo);
        }
        return eliminado;
    }

    public boolean relacionarVideojuegos(int codigoUno, int codigoDos, double afinidad) {
        if (arbol.buscar(codigoUno) == null || arbol.buscar(codigoDos) == null) {
            return false;
        }
        return grafo.agregarArista(codigoUno, codigoDos, afinidad);
    }

    public RutaMinima rutaRecomendacion(int origen, int destino) {
        if (arbol.buscar(origen) == null || arbol.buscar(destino) == null) {
            return new RutaMinima();
        }
        return grafo.dijkstra(origen, destino);
    }

    public List<Integer> explorarRelacionados(int origen) {
        return grafo.recorridoBFS(origen);
    }

    public List<Arista> vecinosDe(int codigo) {
        return grafo.obtenerVecinos(codigo);
    }

    public List<Videojuego> listarInorden() {
        return arbol.recorridoInorden();
    }

    public List<Videojuego> listarPreorden() {
        return arbol.recorridoPreorden();
    }

    public List<Videojuego> listarPostorden() {
        return arbol.recorridoPostorden();
    }

    public int cantidadVideojuegos() {
        return arbol.getCantidad();
    }

    public boolean estaVacio() {
        return arbol.estaVacio();
    }
}
