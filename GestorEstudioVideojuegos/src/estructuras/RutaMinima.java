package estructuras;

import java.util.ArrayList;
import java.util.List;

public class RutaMinima {
    private List<Integer> camino;
    private double costoTotal;

    public RutaMinima() {
        this.camino = new ArrayList<>();
        this.costoTotal = 0.0;
    }

    public List<Integer> getCamino() {
        return camino;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCamino(List<Integer> camino) {
        this.camino = camino;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public boolean existeCamino() {
        return !camino.isEmpty();
    }
}
