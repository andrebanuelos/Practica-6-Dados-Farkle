package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Farkle {
    public static final int TOTAL_DADOS = 6;
    public static final int PUNTOS_PARA_GANAR = 10000;

    private final List<Jugador> jugadores = new ArrayList<>();
    private final List<Dado> dadosEnMesa = new ArrayList<>();
    private final Puntuador puntuador = new Puntuador();

    private int indiceActual;
    private int puntosTurno;
    private int dadosDisponibles;
    private boolean yaAparto;
    private boolean yaTiro;
    private boolean rondaFinal;
    private int indiceQueLlegoA10000 = -1;
    private boolean terminado;
    private String ultimoMensaje = "";

    // ---------------- Preparación ----------------

    public void agregarJugador(Jugador jugador) {

        jugadores.add(jugador);
    }

    // Arranca la partida. Si solo hay un jugador humano, se agrega la computadora
    public void iniciar() {
        // LAMBDA: cuenta cuántos humanos hay
        long humanos = jugadores.stream().filter(Jugador::esHumano).count();
        if (jugadores.size() == 1 && humanos == 1) {
            jugadores.add(new JugadorComputadora("Computadora"));
        }
        if (jugadores.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores.");
        }
        indiceActual = 0;
        iniciarTurno();
    }

    private void iniciarTurno() {
        puntosTurno = 0;
        dadosDisponibles = TOTAL_DADOS;
        dadosEnMesa.clear();
        yaTiro = false;
        yaAparto = false;
    }

    // Acciones del jugador

    //Tira los dados disponibles.

    public boolean tirarDados() {
        if (terminado) throw new IllegalStateException("El juego ya terminó.");
        if (yaTiro && !yaAparto) {
            throw new IllegalStateException("Primero aparta al menos un dado con puntos.");
        }
        dadosEnMesa.clear();
        for (int i = 0; i < dadosDisponibles; i++) {
            Dado d = new Dado();
            d.lanzar();
            dadosEnMesa.add(d);
        }
        yaTiro = true;
        yaAparto = false;

        if (!puntuador.tienePuntos(getValoresEnMesa())) {
            ultimoMensaje = "Salió " + getValoresEnMesa() + " -> ¡FARKLE! " + getJugadorActual().getNombre()
                    + " pierde los " + puntosTurno + " puntos del turno.";
            puntosTurno = 0;
            pasarAlSiguiente();
            return false;
        }
        ultimoMensaje = "";
        return true;
    }

    // Aparta los dados de las posiciones indicadas (empezando en 0).

    public int apartarDados(Set<Integer> posiciones) {
        if (!yaTiro) throw new IllegalStateException("Primero tira los dados.");
        if (posiciones.isEmpty()) throw new IllegalArgumentException("Debes elegir al menos un dado.");
        // LAMBDA: verifica que todas las posiciones existan
        if (posiciones.stream().anyMatch(p -> p < 0 || p >= dadosEnMesa.size())) {
            throw new IllegalArgumentException("Posición de dado inválida.");
        }
        //  obtiene los valores de los dados elegidos
        List<Integer> elegidos = posiciones.stream()
                .map(p -> dadosEnMesa.get(p).getValorVisible())
                .toList();

        int puntos = puntuador.puntuar(elegidos);
        if (puntos == 0) {
            throw new IllegalArgumentException("Esos dados no suman puntos (todos deben sumar).");
        }

        puntosTurno += puntos;
        dadosDisponibles -= posiciones.size();

        // Quita de la mesa los dados apartados.
        List<Dado> restantes = new ArrayList<>();
        for (int i = 0; i < dadosEnMesa.size(); i++) {
            if (!posiciones.contains(i)) restantes.add(dadosEnMesa.get(i));
        }
        dadosEnMesa.clear();
        dadosEnMesa.addAll(restantes);

        if (dadosDisponibles == 0) {
            dadosDisponibles = TOTAL_DADOS;   // "dados calientes"
            ultimoMensaje = "¡Dados calientes! Puedes tirar los 6 dados otra vez.";
        } else {
            ultimoMensaje = "";
        }
        yaAparto = true;
        return puntos;
    }

    // El jugador se planta y guarda los puntos del turno
    public void plantarse() {
        if (!yaAparto) throw new IllegalStateException("Debes apartar dados antes de plantarte.");
        Jugador actual = getJugadorActual();
        actual.sumarPuntos(puntosTurno);
        ultimoMensaje = actual.getNombre() + " se planta y guarda " + puntosTurno
                + " puntos. Total: " + actual.getPuntuacion();

        if (!rondaFinal && actual.getPuntuacion() >= PUNTOS_PARA_GANAR) {
            rondaFinal = true;
            indiceQueLlegoA10000 = indiceActual;
            ultimoMensaje += "\n¡" + actual.getNombre() + " llegó a " + PUNTOS_PARA_GANAR
                    + "! Los demás tienen un último turno.";
        }
        pasarAlSiguiente();
    }

    // Juega el turno completo de la computadora.

    public List<String> jugarTurnoComputadora() {
        List<String> bitacora = new ArrayList<>();
        JugadorComputadora compu = (JugadorComputadora) getJugadorActual();

        while (true) {
            boolean hayPuntos = tirarDados();
            if (!hayPuntos) {
                bitacora.add(ultimoMensaje);
                return bitacora;
            }
            bitacora.add("Tira: " + getValoresEnMesa());
            Set<Integer> eleccion = compu.elegirDados(getValoresEnMesa(), puntuador);
            // LAMBDA: arma el texto de los dados apartados
            List<Integer> valores = eleccion.stream().map(p -> dadosEnMesa.get(p).getValorVisible()).toList();
            int puntos = apartarDados(eleccion);
            bitacora.add("Aparta " + valores + " = " + puntos + " pts (turno: " + puntosTurno + ")");

            if (!compu.quiereSeguirTirando(puntosTurno, dadosDisponibles)) {
                plantarse();
                bitacora.add(ultimoMensaje);
                return bitacora;
            }
        }
    }

    // Control de turnos

    private void pasarAlSiguiente() {
        indiceActual = (indiceActual + 1) % jugadores.size();
        if (rondaFinal && indiceActual == indiceQueLlegoA10000) {
            terminado = true;   // ya todos tuvieron su último turno
        }
        iniciarTurno();
    }

    // Consultas del estado

    public Jugador getJugadorActual() {
        return jugadores.get(indiceActual);
    }

    public List<Jugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    //Jugadores ordenados de mayor a menor puntuación
    public List<Jugador> getTablaPosiciones() {
        List<Jugador> tabla = new ArrayList<>(jugadores);
        // LAMBDA: ordena por puntuación descendente
        tabla.sort(Comparator.comparingInt(Jugador::getPuntuacion).reversed());
        return tabla;
    }

    public Jugador getGanador() {
        return terminado ? getTablaPosiciones().get(0) : null;
    }

    public List<Dado> getDadosEnMesa() {
        return new ArrayList<>(dadosEnMesa);
    }

    public List<Integer> getValoresEnMesa() {
        // LAMBDA (referencia a método): convierte cada Dado en su valor
        return dadosEnMesa.stream().map(Dado::getValorVisible).toList();
    }

    public int getPuntosTurno() { return puntosTurno; }
    public int getDadosDisponibles() { return dadosDisponibles; }
    public boolean puedeTirar() { return !yaTiro || yaAparto; }
    public boolean puedePlantarse() { return yaAparto; }
    public boolean isRondaFinal() { return rondaFinal; }
    public boolean isTerminado() { return terminado; }
    public String getUltimoMensaje() { return ultimoMensaje; }
}

