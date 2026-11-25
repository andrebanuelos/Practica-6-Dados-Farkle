package org.example;


public abstract class Jugador {
    private final String nombre;
    private int puntuacion;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.puntuacion = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void sumarPuntos(int puntos) {
        puntuacion += puntos;
    }

    // true si es una persona; false si es la computadora
    public abstract boolean esHumano();

    @Override
    public String toString() {
        return nombre + " (" + puntuacion + " pts)";
    }
}

