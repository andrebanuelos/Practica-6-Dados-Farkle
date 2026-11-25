package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class ConsolaFarkle {
    private final Scanner sc = new Scanner(System.in);
    private final Farkle juego = new Farkle();

    public void ejecutar() {
        System.out.println("===== FARKLE =====");
        registrarJugadores();
        juego.iniciar();

        while (!juego.isTerminado()) {
            Jugador actual = juego.getJugadorActual();
            mostrarMarcador();
            System.out.println("\n>>> Turno de " + actual.getNombre());

            if (actual.esHumano()) {
                turnoHumano();
            } else {
                // LAMBDA: imprime cada línea de la bitácora
                juego.jugarTurnoComputadora().forEach(linea -> System.out.println("   " + linea));
            }
        }

        System.out.println("\n===== FIN DEL JUEGO =====");
        mostrarMarcador();
        System.out.println("¡Ganó " + juego.getGanador().getNombre() + "!");
    }

    private void registrarJugadores() {
        int humanos = leerEntero("¿Cuántos jugadores humanos? (0-6): ", 0, 6);
        for (int i = 1; i <= humanos; i++) {
            System.out.print("Nombre del jugador " + i + ": ");
            juego.agregarJugador(new JugadorHumano(sc.nextLine().trim()));
        }
        int minCompus = (humanos == 0) ? 2 : 0;
        int compus = leerEntero("¿Cuántos jugadores computadora? (" + minCompus + "-4): ", minCompus, 4);
        for (int i = 1; i <= compus; i++) {
            juego.agregarJugador(new JugadorComputadora("Compu " + i));
        }
        if (humanos == 1 && compus == 0) {
            System.out.println("Solo hay un jugador humano: se agrega la computadora automáticamente.");
        }
    }

    private void turnoHumano() {
        Jugador actual = juego.getJugadorActual();
        while (juego.getJugadorActual() == actual && !juego.isTerminado()) {
            System.out.println("Puntos del turno: " + juego.getPuntosTurno()
                    + " | Dados para tirar: " + juego.getDadosDisponibles());
            System.out.print("Presiona ENTER para tirar...");
            sc.nextLine();

            if (!juego.tirarDados()) {
                System.out.println(juego.getUltimoMensaje());
                return;
            }
            mostrarDados();
            pedirDadosAApartar();
            if (!juego.getUltimoMensaje().isEmpty()) {
                System.out.println(juego.getUltimoMensaje());
            }

            System.out.print("¿(T)irar otra vez o (P)lantarse? ");
            String op = sc.nextLine().trim().toUpperCase();
            if (op.startsWith("P")) {
                juego.plantarse();
                System.out.println(juego.getUltimoMensaje());
                return;
            }
        }
    }

    private void pedirDadosAApartar() {
        while (true) {
            System.out.print("Escribe las posiciones de los dados a apartar (ej: 1 3 4): ");
            String[] partes = sc.nextLine().trim().split("\\s+");
            Set<Integer> posiciones = new HashSet<>();
            try {
                for (String p : partes) {
                    posiciones.add(Integer.parseInt(p) - 1); // el usuario cuenta desde 1
                }
                int puntos = juego.apartarDados(posiciones);
                System.out.println("+" + puntos + " puntos. Turno: " + juego.getPuntosTurno());
                return;
            } catch (NumberFormatException e) {
                System.out.println("Escribe solo números separados por espacios.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void mostrarDados() {
        List<Dado> dados = juego.getDadosEnMesa();
        StringBuilder pos = new StringBuilder("Posición: ");
        StringBuilder val = new StringBuilder("Dados:    ");
        for (int i = 0; i < dados.size(); i++) {
            pos.append(String.format(" %-3d", i + 1));
            val.append(String.format(" %-3s", "[" + dados.get(i).getValorVisible() + "]"));
        }
        System.out.println(pos);
        System.out.println(val);
    }

    private void mostrarMarcador() {
        System.out.println("\n--- Marcador ---" + (juego.isRondaFinal() ? " (RONDA FINAL)" : ""));
        // LAMBDA: imprime cada jugador de la tabla
        juego.getTablaPosiciones().forEach(j ->
                System.out.println("  " + j.getNombre() + ": " + j.getPuntuacion()));
    }

    private int leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            try {
                int n = Integer.parseInt(sc.nextLine().trim());
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) { }
            System.out.println("Número inválido.");
        }
    }
}

