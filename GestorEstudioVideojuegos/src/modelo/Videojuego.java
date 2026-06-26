package modelo;

public class Videojuego {
    private int codigo;
    private String titulo;
    private String genero;
    private int anio;
    private double calificacion;

    public Videojuego(int codigo, String titulo, String genero, int anio, double calificacion) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.genero = genero;
        this.anio = anio;
        this.calificacion = calificacion;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getGenero() {
        return genero;
    }

    public int getAnio() {
        return anio;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setAnio(int anio) {
        if (anio < 0) {
            throw new IllegalArgumentException(
                    "El año no puede ser negativo (recibido: " + anio + ")"
            );
        }
        this.anio = anio;
    }
    public void setCalificacion(double calificacion) {
        if (calificacion < 1.0 || calificacion > 10.0) {
            throw new IllegalArgumentException(
                    "La calificación debe estar entre 1.0 y 10.0 (recibido: " + calificacion + ")"
            );
        }
        this.calificacion = calificacion;
    }

    @Override
    public String toString() {
        return "[" + codigo + "] " + titulo + " | " + genero + " | " + anio + " | nota " + calificacion;
    }
}
