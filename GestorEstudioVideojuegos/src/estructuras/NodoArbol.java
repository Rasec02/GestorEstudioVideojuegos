package estructuras;

import modelo.Videojuego;

public class NodoArbol {
    Videojuego videojuego;
    NodoArbol izquierdo;
    NodoArbol derecho;

    public NodoArbol(Videojuego videojuego) {
        this.videojuego = videojuego;
        this.izquierdo = null;
        this.derecho = null;
    }

    public Videojuego getVideojuego() {
        return videojuego;
    }

    public void setVideojuego(Videojuego videojuego) {
        this.videojuego = videojuego;
    }

    public NodoArbol getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoArbol izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoArbol getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoArbol derecho) {
        this.derecho = derecho;
    }
}
