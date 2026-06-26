package estructuras;

import modelo.Videojuego;
import java.util.ArrayList;
import java.util.List;

public class ArbolBinarioBusqueda {
    private NodoArbol raiz;
    private int cantidad;

    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.cantidad = 0;
    }

    public boolean insertar(Videojuego videojuego) {
        if (buscar(videojuego.getCodigo()) != null) {
            return false;
        }
        raiz = insertarRecursivo(raiz, videojuego);
        cantidad++;
        return true;
    }

    private NodoArbol insertarRecursivo(NodoArbol actual, Videojuego videojuego) {
        if (actual == null) {
            return new NodoArbol(videojuego);
        }
        if (videojuego.getCodigo() < actual.videojuego.getCodigo()) {
            actual.izquierdo = insertarRecursivo(actual.izquierdo, videojuego);
        } else {
            actual.derecho = insertarRecursivo(actual.derecho, videojuego);
        }
        return actual;
    }

    public Videojuego buscar(int codigo) {
        NodoArbol actual = raiz;
        while (actual != null) {
            if (codigo == actual.videojuego.getCodigo()) {
                return actual.videojuego;
            } else if (codigo < actual.videojuego.getCodigo()) {
                actual = actual.izquierdo;
            } else {
                actual = actual.derecho;
            }
        }
        return null;
    }

    public boolean modificar(int codigo, String titulo, String genero, int anio, double calificacion) {
        Videojuego encontrado = buscar(codigo);
        if (encontrado == null) {
            return false;
        }
        encontrado.setTitulo(titulo);
        encontrado.setGenero(genero);
        encontrado.setAnio(anio);
        encontrado.setCalificacion(calificacion);
        return true;
    }

    public boolean eliminar(int codigo) {
        if (buscar(codigo) == null) {
            return false;
        }
        raiz = eliminarRecursivo(raiz, codigo);
        cantidad--;
        return true;
    }

    private NodoArbol eliminarRecursivo(NodoArbol actual, int codigo) {
        if (actual == null) {
            return null;
        }
        if (codigo < actual.videojuego.getCodigo()) {
            actual.izquierdo = eliminarRecursivo(actual.izquierdo, codigo);
        } else if (codigo > actual.videojuego.getCodigo()) {
            actual.derecho = eliminarRecursivo(actual.derecho, codigo);
        } else {
            if (actual.izquierdo == null) {
                return actual.derecho;
            } else if (actual.derecho == null) {
                return actual.izquierdo;
            }
            NodoArbol sucesor = encontrarMinimo(actual.derecho);
            actual.videojuego = sucesor.videojuego;
            actual.derecho = eliminarRecursivo(actual.derecho, sucesor.videojuego.getCodigo());
        }
        return actual;
    }

    private NodoArbol encontrarMinimo(NodoArbol nodo) {
        while (nodo.izquierdo != null) {
            nodo = nodo.izquierdo;
        }
        return nodo;
    }

    public List<Videojuego> recorridoInorden() {
        List<Videojuego> lista = new ArrayList<>();
        inorden(raiz, lista);
        return lista;
    }

    private void inorden(NodoArbol actual, List<Videojuego> lista) {
        if (actual != null) {
            inorden(actual.izquierdo, lista);
            lista.add(actual.videojuego);
            inorden(actual.derecho, lista);
        }
    }

    public List<Videojuego> recorridoPreorden() {
        List<Videojuego> lista = new ArrayList<>();
        preorden(raiz, lista);
        return lista;
    }

    private void preorden(NodoArbol actual, List<Videojuego> lista) {
        if (actual != null) {
            lista.add(actual.videojuego);
            preorden(actual.izquierdo, lista);
            preorden(actual.derecho, lista);
        }
    }

    public List<Videojuego> recorridoPostorden() {
        List<Videojuego> lista = new ArrayList<>();
        postorden(raiz, lista);
        return lista;
    }

    private void postorden(NodoArbol actual, List<Videojuego> lista) {
        if (actual != null) {
            postorden(actual.izquierdo, lista);
            postorden(actual.derecho, lista);
            lista.add(actual.videojuego);
        }
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean estaVacio() {
        return raiz == null;
    }
}
