package estructuras;

public class Arista {
    private int destino;
    private double peso;

    public Arista(int destino, double peso) {
        this.destino = destino;
        this.peso = peso;
    }

    public int getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }
}
