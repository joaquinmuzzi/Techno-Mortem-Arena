package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.entidades.EstadoArmas;
import com.et35.technomortemarena.partida.EstadoPartida;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.function.LongSupplier;

public final class ServidorSala implements AutoCloseable {
    private static final int LARGO_ENTRADA = 4 + 4 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + 2 + 2;

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
        if (paquete.remaining() < 2 * Long.BYTES) {
            return;
        }
        long faltantesBajas = paquete.getLong();
        long faltantesAltas = paquete.getLong();
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
            boolean falta = i < 64 ? (faltantesBajas & (1L << i)) != 0 : (faltantesAltas & (1L << (i - 64))) != 0;
            if (!falta) {
                continue;
            }
            ByteBuffer fila = Protocolo.nuevo(Protocolo.FILA_MAPA);
            fila.put((byte) i);
            fila.putShort((short) filasCodificadas[i].length);
            fila.put(filasCodificadas[i]);
            Protocolo.enviar(canal, fila, origen);
        }
    }

    private void atenderEntrada(SocketAddress origen, ByteBuffer paquete) {
        if (paquete.remaining() < LARGO_ENTRADA || cliente == null || !cliente.equals(origen)) {
            return;
        }
        int recibido = paquete.getInt();
        if (recibido != token) {
            return;
        }
        int secuencia = paquete.getInt();
        int banderas = paquete.get();
        byte salto = paquete.get();
        byte estocada = paquete.get();
        byte rodar = paquete.get();
        byte lanzar = paquete.get();
        int eleccionPieza = paquete.get();
        byte colocar = paquete.get();
        float apuntaX = paquete.getShort();
        float apuntaY = paquete.getShort();
        entrada.aplicar(secuencia, (banderas & 1) != 0, (banderas & 2) != 0, (banderas & 4) != 0,
            salto, estocada, rodar, lanzar, eleccionPieza, colocar, apuntaX, apuntaY);
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

    public boolean hayCliente() {
        return cliente != null;
    }

    public boolean clienteListo() {
        return listo;
    }

    public boolean consumirDesconexion() {
        boolean fue = desconexionPendiente;
        desconexionPendiente = false;
        return fue;
    }

    public FuenteEntrada getEntradaRemota() {
        return entrada;
    }

    public void enviarEstado(EstadoJugador anfitrion, EstadoJugador remoto, EstadoPartida partida) {
        enviarEstado(anfitrion, remoto, partida, new EstadoArmas());
    }

    public void enviarEstado(EstadoJugador anfitrion, EstadoJugador remoto, EstadoPartida partida, EstadoArmas armas) {
        if (cliente == null || !listo) {
            return;
        }
        ByteBuffer paquete = Protocolo.nuevo(Protocolo.ESTADO);
        paquete.putInt(++secuenciaEstado);
        CodecEstado.escribir(paquete, anfitrion);
        CodecEstado.escribir(paquete, remoto);
        CodecEstado.escribir(paquete, partida);
        CodecEstado.escribir(paquete, armas);
        Protocolo.enviar(canal, paquete, cliente);
    }

    @Override
    public void close() {
        if (cliente != null) {
            for (int i = 0; i < 2; i++) {
                ByteBuffer adios = Protocolo.nuevo(Protocolo.ADIOS);
                adios.putInt(token);
                Protocolo.enviar(canal, adios, cliente);
            }
        }
        try {
            canal.close();
        } catch (IOException e) {
        }
    }
}
