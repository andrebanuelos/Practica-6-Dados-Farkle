package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JugadorComputadora extends Jugador {
    private static final int PUNTOS_MINIMOS_PARA_PLANTARSE = 350;

    public JugadorComputadora(String nombre)
    {
        super(nombre);
    }

    @Override
    public boolean esHumano() {

        return false;
    }

    //Elige la combinación de dados que da más puntos
    public Set<Integer> elegirDados(List<Integer> valores, Puntuador puntuador) {
        return new HashSet<>(puntuador.mejorSeleccion(valores));
    }

    // Decide si vuelve a tirar o se planta
    public boolean quiereSeguirTirando(int puntosTurno, int dadosRestantes) {
        if (dadosRestantes == 6) {
            return true;
        }
        if (puntosTurno < PUNTOS_MINIMOS_PARA_PLANTARSE) {
            return true;
        }
        return dadosRestantes >= 4 && puntosTurno < 600;
    }
}