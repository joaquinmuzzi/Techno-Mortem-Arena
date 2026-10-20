package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.function.LongSupplier;

/**
 * El lado del anfitrion: recibe la entrada del otro jugador y le manda el estado de la partida.
 *
 * <p>El anfitrion es autoritativo: simula todo el juego y los clientes solo mandan sus teclas. La
 * sala admite {@link Protocolo#MAX_JUGADORES} jugadores contando al anfitrion, o sea un cliente.
 *
 * <p>No usa hilos: el socket es no bloqueante y {@link #sondear()} se llama una vez por fotograma
 * desde el bucle del juego, asi que todo corre en el mismo hilo que la simulacion.
 *
 * <p>Seguridad: al unirse, el cliente recibe un token al azar que tiene que repetir en cada
 * entrada, y los paquetes solo valen si vienen de su misma direccion. Cualquier paquete mal
 * formado se descarta. La sala escucha en todas las interfaces: en una red local la ve cualquiera
 * que conozca la direccion y el puerto.
 */
public final class ServidorSala implements AutoCloseable {

    private final DatagramChannel canal;
    private final byte[][] filasCodificadas;
    private final LongSupplier reloj;
    private final SecureRandom azar = new SecureRandom();
    private final ByteBuffer entrante = ByteBuffer.allocate(Protocolo.MAX_DATAGRAMA);

    private SocketAddress cliente;
    private int token;
    private long ultimoMensaje;
    private long ultimaRespuesta = Long.MIN_VALUE / 2;
    private boolean listo;
    private boolean desconexionPendiente;
    private EntradaRemota entrada = new EntradaRemota();
    private int secuenciaEstado;

    private ServidorSala(DatagramChannel canal, byte[][] filasCodificadas, LongSupplier reloj) {
        this.canal = canal;
        this.filasCodificadas = filasCodificadas;
        this.reloj = reloj;
    }

    /**
     * Abre la sala.
     *
     * @param puerto puerto UDP en el que escuchar; con 0 el sistema elige uno libre (lo usan los tests)
     * @param filas  filas del mapa que se les manda a los clientes ({@link Arena#getFilas()})
     * @throws IOException si no se puede usar el puerto, por ejemplo porque ya hay otra sala abierta
     */
    public static ServidorSala abrir(int puerto, String[] filas, LongSupplier reloj) throws IOException {
        if (filas.length < 1 || filas.length > Protocolo.MAX_FILAS) {
            throw new IllegalArgumentException("El mapa debe tener entre 1 y " + Protocolo.MAX_FILAS + " filas");
        }
        byte[][] codificadas = new byte[filas.length][];
        for (int i = 0; i < filas.length; i++) {
            codificadas[i] = CodecMapa.codificarFila(filas[i]);
            if (CodecMapa.decodificarFila(codificadas[i], Arena.COLUMNAS) == null) {
                throw new IllegalArgumentException("La fila " + i + " del mapa no es valida");
            }
        }
        DatagramChannel canal = DatagramChannel.open();
        try {
            canal.bind(new InetSocketAddress(puerto));
            canal.configureBlocking(false);
        } catch (IOException | RuntimeException e) {
            canal.close();
            throw e;
        }
        return new ServidorSala(canal, codificadas, reloj);
    }

    public int getPuerto() {
        try {
            return ((InetSocketAddress) canal.getLocalAddress()).getPort();
        } catch (IOException e) {
            return -1;
        }
    }

    /** Lee todo lo que llego desde el ultimo fotograma. Hay que llamarlo una vez por fotograma. */
    public void sondear() {
        while (true) {
            entrante.clear();
            SocketAddress origen;
            try {
                origen = canal.receive(entrante);
            } catch (IOException e) {
                break;
            }
            if (origen == null) {
                break;
            }
            entrante.flip();
            try {
                procesar(origen, entrante);
            } catch (RuntimeException e) {
                // Paquete mal formado: se ignora, no puede tumbar el juego.
            }
        }
        if (cliente != null && reloj.getAsLong() - ultimoMensaje > Protocolo.TIEMPO_SIN_NOTICIAS_MS) {
            liberarCliente();
        }
    }

    private void procesar(SocketAddress origen, ByteBuffer paquete) {
        switch (Protocolo.leerTipo(paquete)) {
            case Protocolo.HELLO:
                atenderHello(origen, paquete);
                break;
            case Protocolo.ENTRADA:
                atenderEntrada(origen, paquete);
                break;
            case Protocolo.ADIOS:
                atenderAdios(origen, paquete);
                break;
            default:
                break;
        }
    }

    private void atenderHello(SocketAddress origen, ByteBuffer paquete) {
        if (paquete.remaining() < Long.BYTES) {
            return;
        }
        long faltantes = paquete.getLong();
        long ahora = reloj.getAsLong();
        if (cliente == null) {
            cliente = origen;
            int nuevo;
            do {
                nuevo = azar.nextInt();
            } while (nuevo == 0);
            token = nuevo;
            listo = false;
            entrada = new EntradaRemota();
            secuenciaEstado = 0;
        } else if (!cliente.equals(origen)) {
            Protocolo.enviar(canal, Protocolo.nuevo(Protocolo.SALA_LLENA), origen);
            return;
        }
        ultimoMensaje = ahora;
        if (ahora - ultimaRespuesta < Protocolo.PAUSA_ENTRE_RESPUESTAS_MS) {
            return;
        }
        ultimaRespuesta = ahora;

        ByteBuffer bienvenida = Protocolo.nuevo(Protocolo.BIENVENIDA);
        bienvenida.put((byte) 1);
        bienvenida.putInt(token);
        bienvenida.put((byte) filasCodificadas.length);
        Protocolo.enviar(canal, bienvenida, origen);
        for (int i = 0; i < filasCodificadas.length; i++) {
            if ((faltantes & (1L << i)) == 0) {
                continue;
            }
            ByteBuffer fila = Protocolo.nuevo(Protocolo.FILA_MAPA);
            fila.put((byte) i);
            fila.put((byte) filasCodificadas[i].length);
            fila.put(filasCodificadas[i]);
            Protocolo.enviar(canal, fila, origen);
        }
    }

    private void atenderEntrada(SocketAddress origen, ByteBuffer paquete) {
        if (paquete.remaining() < 4 + 4 + 1 + 3 || cliente == null || !cliente.equals(origen)) {
            return;
        }
        int recibido = paquete.getInt();
        if (recibido != token) {
            return;
        }
        int secuencia = paquete.getInt();
        int banderas = paquete.get();
        byte salto = paquete.get();
        byte subir = paquete.get();
        byte bajar = paquete.get();
        entrada.aplicar(secuencia, (banderas & 1) != 0, (banderas & 2) != 0, (banderas & 4) != 0,
            salto, subir, bajar);
        ultimoMensaje = reloj.getAsLong();
        listo = true;
    }

    private void atenderAdios(SocketAddress origen, ByteBuffer paquete) {
        if (paquete.remaining() < 4 || cliente == null || !cliente.equals(origen)) {
            return;
        }
        if (paquete.getInt() == token) {
            liberarCliente();
        }
    }

    private void liberarCliente() {
        if (listo) {
            desconexionPendiente = true;
        }
        cliente = null;
        listo = false;
        entrada = new EntradaRemota();
    }

    /** Hay alguien que pidio unirse (aunque todavia no haya terminado de bajar el mapa). */
    public boolean hayCliente() {
        return cliente != null;
    }

    /** El cliente ya bajo el mapa y esta mandando su entrada: hay que simular a su jugador. */
    public boolean clienteListo() {
        return listo;
    }

    /** Verdadero una sola vez despues de que un cliente que ya estaba jugando se va o se cae. */
    public boolean consumirDesconexion() {
        boolean fue = desconexionPendiente;
        desconexionPendiente = false;
        return fue;
    }

    /** La entrada del jugador remoto, para pasarsela a su {@code Jugador}. */
    public FuenteEntrada getEntradaRemota() {
        return entrada;
    }

    /**
     * Le manda al cliente el estado de la partida: el del anfitrion (indice 0) y el de el (indice 1).
     * No hace nada si el cliente todavia no esta listo.
     */
    public void enviarEstado(EstadoJugador anfitrion, EstadoJugador remoto) {
        if (cliente == null || !listo) {
            return;
        }
        ByteBuffer paquete = Protocolo.nuevo(Protocolo.ESTADO);
        paquete.putInt(++secuenciaEstado);
        CodecEstado.escribir(paquete, anfitrion);
        CodecEstado.escribir(paquete, remoto);
        Protocolo.enviar(canal, paquete, cliente);
    }

    @Override
    public void close() {
        if (cliente != null) {
            // Dos veces, porque en UDP un solo aviso puede perderse.
            for (int i = 0; i < 2; i++) {
                ByteBuffer adios = Protocolo.nuevo(Protocolo.ADIOS);
                adios.putInt(token);
                Protocolo.enviar(canal, adios, cliente);
            }
        }
        try {
            canal.close();
        } catch (IOException e) {
            // Ya se esta cerrando: no hay nada mas que hacer.
        }
    }
}
