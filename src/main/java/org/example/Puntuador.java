package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Puntuador {

    // los conteos (cara -> cuántas veces salió) y regresa sus puntos (0 si no aplica).
    private final List<Function<Map<Integer, Long>, Integer>> reglasSeisDados = new ArrayList<>();

    public Puntuador() {
        // Escalera 1-2-3-4-5-6
        reglasSeisDados.add(c -> c.size() == 6 ? 1500 : 0);
        // Tres pares
        reglasSeisDados.add(c -> c.values().stream().filter(n -> n == 2).count() == 3 ? 1500 : 0);
        // Cuatro iguales + un par
        reglasSeisDados.add(c -> c.containsValue(4L) && c.containsValue(2L) ? 1500 : 0);
        // Dos tercias
        reglasSeisDados.add(c -> c.values().stream().filter(n -> n == 3).count() == 2 ? 2500 : 0);
    }

    public int puntuar(List<Integer> valores) {
        if (valores.isEmpty()) {
            return 0;
        }
        //  cuenta cuántas veces aparece cada cara
        Map<Integer, Long> conteo = valores.stream()
                .collect(Collectors.groupingBy(v -> v, HashMap::new, Collectors.counting()));

        int puntos = puntuarGeneral(conteo);

        if (valores.size() == 6) {
            for (Function<Map<Integer, Long>, Integer> regla : reglasSeisDados) {
                puntos = Math.max(puntos, regla.apply(conteo));
            }
        }
        return puntos;
    }

    // Reglas normales: unos, cincos y grupos de 3 o más iguales
    private int puntuarGeneral(Map<Integer, Long> conteo) {
        int total = 0;
        for (Map.Entry<Integer, Long> e : conteo.entrySet()) {
            int cara = e.getKey();
            int veces = e.getValue().intValue();
            int sueltoValor = (cara == 1) ? 100 : (cara == 5) ? 50 : 0;

            if (veces >= 3) {
                // Se toma lo que convenga más: el grupo completo o tercia + sueltos
                int grupo = puntosGrupo(cara, veces);
                int terciaMasSueltos = puntosGrupo(cara, 3) + (veces - 3) * sueltoValor;
                total += Math.max(grupo, terciaMasSueltos);
            } else if (sueltoValor > 0) {
                total += veces * sueltoValor;
            } else {
                return 0; // hay un dado que no suma puntos
            }
        }
        return total;
    }

    private int puntosGrupo(int cara, int veces) {
        return switch (veces) {
            case 3 -> (cara == 1) ? 1000 : cara * 100;
            case 4 -> 1000;
            case 5 -> 2000;
            default -> 3000;
        };
    }


    //Busca la mejor combinación posible dentro de una tirada.

    public List<Integer> mejorSeleccion(List<Integer> valores) {
        int n = valores.size();
        List<Integer> mejor = new ArrayList<>();
        int mejoresPuntos = 0;
        // Prueba todas las combinaciones posibles de dados
        for (int mascara = 1; mascara < (1 << n); mascara++) {
            List<Integer> posiciones = new ArrayList<>();
            List<Integer> elegidos = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mascara & (1 << i)) != 0) {
                    posiciones.add(i);
                    elegidos.add(valores.get(i));
                }
            }
            int p = puntuar(elegidos);
            if (p > mejoresPuntos) {
                mejoresPuntos = p;
                mejor = posiciones;
            }
        }
        return mejor;
    }

    // true si en la tirada hay al menos una combinación con puntos.
    public boolean tienePuntos(List<Integer> valores) {

        return !mejorSeleccion(valores).isEmpty();
    }
}
