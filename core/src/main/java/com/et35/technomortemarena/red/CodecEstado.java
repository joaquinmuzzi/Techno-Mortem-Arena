package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.ArmasEnJuego;
import com.et35.technomortemarena.entidades.EstadoArmas;
import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entidades.PowerUp;
import com.et35.technomortemarena.entidades.Proyectil;
import com.et35.technomortemarena.entidades.TipoArma;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.EdicionNivel;
import com.et35.technomortemarena.partida.EstadoPartida;
import com.et35.technomortemarena.partida.PiezaNivel;
import com.et35.technomortemarena.partida.MarcadorPartida;
import com.et35.technomortemarena.partida.RondasPartida;
import java.nio.ByteBuffer;

/**
 * Pasa un {@link EstadoJugador} a bytes y de vuelta ({@link #BYTES} bytes), y lo mismo con el
 * {@link EstadoPartida} ({@link #BYTES_PARTIDA} bytes).
 */
final class CodecEstado {

    static final int BYTES = 4 + 4 + 1 + 1 + 4 + 4 + 4 + 4 + 4 + 1 + 1 + PowerUp.values().length;

    /**
     * Lo minimo de las armas fuera de las manos: las cuatro cantidades (proyectiles, armas en el suelo, mejoras y
     * celdas rotas). Cada uno suma lo suyo.
     */
    static final int BYTES_ARMAS = 4;
    /** Columna y fila de una celda que rompio un cohete. */
    static final int BYTES_POR_CELDA_ROTA = 2;
    static final int BYTES_POR_POWER_UP = 1 + 4 + 4;
    static final int BYTES_POR_PROYECTIL = 1 + 4 + 4 + 4;
    static final int BYTES_POR_ARMA_EN_EL_SUELO = 1 + 4 + 4;

    /**
     * Lo fijo del estado de la partida: etapa, numero de ronda, rondas ganadas de cada uno y ganador de la
     * ultima ronda (un byte cada uno), los dos tiempos, y la edicion del nivel (editor, tres opciones,
     * eleccion, celda del cursor, tiempo y cantidad de piezas). Cada pieza colocada suma
     * {@link #BYTES_POR_PIEZA}.
     */
    static final int BYTES_PARTIDA = 5 + 4 + 4 + (1 + 3 + 1 + 1 + 1 + 4 + 1);
    /** Tipo, columna y fila de una pieza colocada. */
    static final int BYTES_POR_PIEZA = 3;

    private static final int BIT_MIRANDO = 1;
    private static final int BIT_VIVO = 2;
    private static final int BIT_CORRIENDO = 4;
    private static final int BIT_RODANDO = 8;

    private CodecEstado() {
    }

    static void escribir(ByteBuffer buffer, EstadoJugador estado) {
        buffer.putFloat(estado.x);
        buffer.putFloat(estado.y);
        buffer.put((byte) estado.vida);
        int banderas = (estado.mirandoDerecha ? BIT_MIRANDO : 0)
            | (estado.vivo ? BIT_VIVO : 0)
            | (estado.corriendo ? BIT_CORRIENDO : 0)
            | (estado.rodando ? BIT_RODANDO : 0);
        buffer.put((byte) banderas);
        buffer.putFloat(estado.anguloEspada);
        buffer.putFloat(estado.extension);
        buffer.putFloat(estado.tiempoRestanteFlash);
        buffer.putInt(estado.contadorGolpes);
        buffer.putInt(estado.contadorAterrizajes);
        buffer.put((byte) estado.arma);
        buffer.put((byte) estado.balas);
        // el tiempo de cada mejora en decimas de segundo, en un byte sin signo
        for (float tiempo : estado.tiempoPowerUp) {
            buffer.put((byte) Math.max(0, Math.min(255, Math.round(tiempo * 10f))));
        }
    }

    /**
     * Lee un estado del buffer.
     *
     * @return {@code false} si no hay bytes suficientes o algun valor no tiene sentido (un numero no
     *         finito, una extension fuera de 0 a 1): en ese caso {@code destino} no se toca
     */
    static boolean leer(ByteBuffer buffer, EstadoJugador destino) {
        if (buffer.remaining() < BYTES) {
            return false;
        }
        float x = buffer.getFloat();
        float y = buffer.getFloat();
        int vida = buffer.get();
        int banderas = buffer.get();
        float angulo = buffer.getFloat();
        float extension = buffer.getFloat();
        float flash = buffer.getFloat();
        int golpes = buffer.getInt();
        int aterrizajes = buffer.getInt();
        int arma = buffer.get();
        int balas = buffer.get();
        float[] tiempos = new float[PowerUp.values().length];
        for (int i = 0; i < tiempos.length; i++) {
            tiempos[i] = (buffer.get() & 0xFF) / 10f;
        }
        if (arma < 0 || arma > TipoArma.values().length || balas < 0 || balas > TipoArma.MAX_BALAS) {
            return false;
        }
        if (!finito(x) || !finito(y) || !finito(angulo) || !finito(extension) || !finito(flash)
            || vida < 0 || extension < 0f || extension > 1f) {
            return false;
        }
        destino.x = x;
        destino.y = y;
        destino.vida = vida;
        destino.mirandoDerecha = (banderas & BIT_MIRANDO) != 0;
        destino.vivo = (banderas & BIT_VIVO) != 0;
        destino.corriendo = (banderas & BIT_CORRIENDO) != 0;
        destino.rodando = (banderas & BIT_RODANDO) != 0;
        destino.anguloEspada = angulo;
        destino.extension = extension;
        destino.tiempoRestanteFlash = flash;
        destino.contadorGolpes = golpes;
        destino.contadorAterrizajes = aterrizajes;
        destino.arma = arma;
        destino.balas = balas;
        System.arraycopy(tiempos, 0, destino.tiempoPowerUp, 0, tiempos.length);
        return true;
    }

    static void escribir(ByteBuffer buffer, EstadoPartida partida) {
        buffer.put((byte) partida.fase);
        buffer.put((byte) Math.min(partida.numeroRonda, 255));
        buffer.put((byte) partida.rondasGanadasUno);
        buffer.put((byte) partida.rondasGanadasDos);
        buffer.put((byte) partida.ganadorDeLaRonda);
        buffer.putFloat(partida.tiempoRestante);
        buffer.putFloat(partida.tiempoMuerteSubita);
        buffer.put((byte) partida.editor);
        for (int opcion : partida.opciones) {
            buffer.put((byte) opcion);
        }
        buffer.put((byte) partida.eleccion);
        buffer.put((byte) partida.cursorColumna); // sin signo: hay mas de 127 columnas
        buffer.put((byte) partida.cursorFila);
        buffer.putFloat(partida.tiempoEdicion);
        int cantidad = Math.min(partida.piezas.size(), EdicionNivel.MAX_PIEZAS);
        buffer.put((byte) cantidad);
        for (int i = 0; i < cantidad; i++) {
            int[] pieza = partida.piezas.get(i);
            buffer.put((byte) pieza[0]);
            buffer.put((byte) pieza[1]);
            buffer.put((byte) pieza[2]);
        }
    }

    /**
     * Lee el estado de la partida.
     *
     * @return {@code false} si faltan bytes o algun valor no tiene sentido (una etapa que no existe, mas
     *         rondas ganadas de las que alcanzan para ganar, un ganador que no es 0, 1 ni -1): en ese
     *         caso {@code destino} no se toca
     */
    static boolean leer(ByteBuffer buffer, EstadoPartida destino) {
        if (buffer.remaining() < BYTES_PARTIDA) {
            return false;
        }
        int fase = buffer.get();
        int ronda = buffer.get() & 0xFF;
        int uno = buffer.get();
        int dos = buffer.get();
        int ganador = buffer.get();
        float tiempoRestante = buffer.getFloat();
        float tiempoMuerteSubita = buffer.getFloat();
        int editor = buffer.get();
        int[] opciones = {buffer.get(), buffer.get(), buffer.get()};
        int eleccion = buffer.get();
        int cursorColumna = buffer.get() & 0xFF;
        int cursorFila = buffer.get();
        float tiempoEdicion = buffer.getFloat();
        int cantidad = buffer.get();
        if (cantidad < 0 || cantidad > EdicionNivel.MAX_PIEZAS || buffer.remaining() < cantidad * BYTES_POR_PIEZA) {
            return false;
        }
        int[][] piezas = new int[cantidad][];
        for (int i = 0; i < cantidad; i++) {
            piezas[i] = new int[] {buffer.get(), buffer.get() & 0xFF, buffer.get()};
            if (PiezaNivel.porIndice(piezas[i][0]) == null || piezas[i][1] < 0 || piezas[i][1] >= Arena.COLUMNAS
                || piezas[i][2] < 0 || piezas[i][2] >= Arena.FILAS) {
                return false;
            }
        }
        for (int opcion : opciones) {
            if (opcion < -1 || opcion >= PiezaNivel.values().length) {
                return false;
            }
        }
        if (editor < -1 || editor > 1 || eleccion < -1 || eleccion >= EdicionNivel.OPCIONES
            || cursorColumna < 0 || cursorColumna >= Arena.COLUMNAS || cursorFila < 0 || cursorFila >= Arena.FILAS
            || !finito(tiempoEdicion) || tiempoEdicion < 0f) {
            return false;
        }
        int maximo = MarcadorPartida.RONDAS_PARA_GANAR;
        if (fase < 0 || fase >= RondasPartida.Fase.values().length || ronda < 1
            || uno < 0 || uno > maximo || dos < 0 || dos > maximo || ganador < -1 || ganador > 1
            || Float.isNaN(tiempoRestante) || Float.isInfinite(tiempoRestante) || tiempoRestante < 0f
            || Float.isNaN(tiempoMuerteSubita) || Float.isInfinite(tiempoMuerteSubita) || tiempoMuerteSubita < 0f) {
            return false;
        }
        destino.fase = fase;
        destino.numeroRonda = ronda;
        destino.rondasGanadasUno = uno;
        destino.rondasGanadasDos = dos;
        destino.ganadorDeLaRonda = ganador;
        destino.tiempoRestante = tiempoRestante;
        destino.tiempoMuerteSubita = tiempoMuerteSubita;
        destino.editor = editor;
        System.arraycopy(opciones, 0, destino.opciones, 0, opciones.length);
        destino.eleccion = eleccion;
        destino.cursorColumna = cursorColumna;
        destino.cursorFila = cursorFila;
        destino.tiempoEdicion = tiempoEdicion;
        destino.piezas.clear();
        for (int[] pieza : piezas) {
            destino.piezas.add(pieza);
        }
        return true;
    }

    static void escribir(ByteBuffer buffer, EstadoArmas armas) {
        int proyectiles = Math.min(armas.proyectiles.size(), EstadoArmas.MAX_PROYECTILES);
        buffer.put((byte) proyectiles);
        for (int i = 0; i < proyectiles; i++) {
            float[] p = armas.proyectiles.get(i);
            buffer.put((byte) p[0]);
            buffer.putFloat(p[1]);
            buffer.putFloat(p[2]);
            buffer.putFloat(p[3]);
        }
        int enElSuelo = Math.min(armas.enElSuelo.size(), EstadoArmas.MAX_EN_EL_SUELO);
        buffer.put((byte) enElSuelo);
        for (int i = 0; i < enElSuelo; i++) {
            float[] a = armas.enElSuelo.get(i);
            buffer.put((byte) a[0]);
            buffer.putFloat(a[1]);
            buffer.putFloat(a[2]);
        }
        int mejoras = Math.min(armas.powerUps.size(), EstadoArmas.MAX_POWER_UPS);
        buffer.put((byte) mejoras);
        for (int i = 0; i < mejoras; i++) {
            float[] m = armas.powerUps.get(i);
            buffer.put((byte) m[0]);
            buffer.putFloat(m[1]);
            buffer.putFloat(m[2]);
        }
        int rotas = Math.min(armas.celdasRotas.size(), ArmasEnJuego.MAX_CELDAS_ROTAS);
        buffer.put((byte) rotas);
        for (int i = 0; i < rotas; i++) {
            int[] celda = armas.celdasRotas.get(i);
            buffer.put((byte) celda[0]);
            buffer.put((byte) celda[1]);
        }
    }

    /**
     * Lee los proyectiles y las armas en el suelo.
     *
     * @return {@code false} si faltan bytes, hay mas de los que entran o algun valor no tiene sentido (un
     *         tipo que no existe, un numero no finito): en ese caso {@code destino} no se toca
     */
    static boolean leer(ByteBuffer buffer, EstadoArmas destino) {
        if (buffer.remaining() < 1) {
            return false;
        }
        int proyectiles = buffer.get();
        if (proyectiles < 0 || proyectiles > EstadoArmas.MAX_PROYECTILES
            || buffer.remaining() < proyectiles * BYTES_POR_PROYECTIL + 1) {
            return false;
        }
        float[][] leidos = new float[proyectiles][];
        for (int i = 0; i < proyectiles; i++) {
            int tipo = buffer.get();
            leidos[i] = new float[] {tipo, buffer.getFloat(), buffer.getFloat(), buffer.getFloat()};
            if (tipo < 0 || tipo >= Proyectil.Tipo.values().length
                || !finito(leidos[i][1]) || !finito(leidos[i][2]) || !finito(leidos[i][3])) {
                return false;
            }
        }
        int enElSuelo = buffer.get();
        if (enElSuelo < 0 || enElSuelo > EstadoArmas.MAX_EN_EL_SUELO
            || buffer.remaining() < enElSuelo * BYTES_POR_ARMA_EN_EL_SUELO) {
            return false;
        }
        float[][] armas = new float[enElSuelo][];
        for (int i = 0; i < enElSuelo; i++) {
            int tipo = buffer.get();
            armas[i] = new float[] {tipo, buffer.getFloat(), buffer.getFloat()};
            if (TipoArma.porCodigo(tipo) == null || !finito(armas[i][1]) || !finito(armas[i][2])) {
                return false;
            }
        }
        if (buffer.remaining() < 1) {
            return false;
        }
        int cantidadMejoras = buffer.get();
        if (cantidadMejoras < 0 || cantidadMejoras > EstadoArmas.MAX_POWER_UPS
            || buffer.remaining() < cantidadMejoras * BYTES_POR_POWER_UP) {
            return false;
        }
        float[][] mejoras = new float[cantidadMejoras][];
        for (int i = 0; i < cantidadMejoras; i++) {
            int tipo = buffer.get();
            mejoras[i] = new float[] {tipo, buffer.getFloat(), buffer.getFloat()};
            if (PowerUp.porIndice(tipo) == null || !finito(mejoras[i][1]) || !finito(mejoras[i][2])) {
                return false;
            }
        }
        if (buffer.remaining() < 1) {
            return false;
        }
        int cantidadRotas = buffer.get() & 0xFF;
        if (cantidadRotas > ArmasEnJuego.MAX_CELDAS_ROTAS || buffer.remaining() < cantidadRotas * BYTES_POR_CELDA_ROTA) {
            return false;
        }
        int[][] rotas = new int[cantidadRotas][];
        for (int i = 0; i < cantidadRotas; i++) {
            rotas[i] = new int[] {buffer.get() & 0xFF, buffer.get() & 0xFF};
            if (rotas[i][0] >= Arena.COLUMNAS || rotas[i][1] >= Arena.FILAS) {
                return false;
            }
        }
        destino.celdasRotas.clear();
        for (int[] celda : rotas) {
            destino.celdasRotas.add(celda);
        }
        destino.powerUps.clear();
        for (float[] m : mejoras) {
            destino.powerUps.add(m);
        }
        destino.proyectiles.clear();
        for (float[] p : leidos) {
            destino.proyectiles.add(p);
        }
        destino.enElSuelo.clear();
        for (float[] a : armas) {
            destino.enElSuelo.add(a);
        }
        return true;
    }

    private static boolean finito(float valor) {
        return !Float.isNaN(valor) && !Float.isInfinite(valor);
    }
}
