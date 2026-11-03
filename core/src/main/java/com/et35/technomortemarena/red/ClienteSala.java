package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.EstadoPartida;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.LongSupplier;

/**
 * El lado del cliente: se une a la sala de un anfitrion, baja el mapa, le manda sus teclas y recibe
 * el estado de la partida para dibujarlo.
 *
 * <p>Pasa por tres fases: {@link Fase#CONECTANDO} (repite el saludo hasta tener el mapa completo),
 * {@link Fase#JUGANDO} y {@link Fase#DESCONECTADO}, de la que no vuelve. Igual que el servidor, no
 * usa hilos: {@link #sondear()} se llama una vez por fotograma.
 */
public final class ClienteSala implements AutoCloseable {

    public enum Fase { CONECTANDO, JUGANDO, DESCONECTADO }

    private final DatagramChannel canal;
    private final SocketAddress servidor;
    private final LongSupplier reloj;
    private final ByteBuffer entrante = ByteBuffer.allocate(Protocolo.MAX_DATAGRAMA);

    private Fase fase = Fase.CONECTANDO;
    private String motivo = "";
    private final long inicio;
    private long ultimoHello = Long.MIN_VALUE / 2;
    private long ultimoMensaje;

    private boolean bienvenida;
    private int token;
    private int idJugador;
    private String[] filas;

    private final EstadoJugador[] estados = {new EstadoJugador(), new EstadoJugador()};
    private final EstadoPartida estadoPartida = new EstadoPartida();
    private boolean hayEstado;
    private int ultimaSecuenciaEstado;

    private int secuenciaEntrada;
    private byte contadorSalto;
    private byte contadorEstocada;

    private ClienteSala(DatagramChannel canal, SocketAddress servidor, LongSupplier reloj) {
        this.canal = canal;
        this.servidor = servidor;
        this.reloj = reloj;
        this.inicio = reloj.getAsLong();
    }

    /**
     * Empieza a conectarse a una sala. No espera: hay que llamar a {@link #sondear()} cada fotograma y
     * mirar {@link #getFase()}.
     *
     * @throws IOException si la direccion no se puede resolver o no se puede abrir el socket
     */
    public static ClienteSala conectar(InetSocketAddress servidor, LongSupplier reloj) throws IOException {
        if (servidor.isUnresolved()) {
            throw new IOException("No se encontro la direccion " + servidor.getHostString());
        }
        DatagramChannel canal = DatagramChannel.open();
        try {
            canal.configureBlocking(false);
        } catch (IOException | RuntimeException e) {
            canal.close();
            throw e;
        }
        return new ClienteSala(canal, servidor, reloj);
    }

    /** Lee lo que llego, avanza la conexion y detecta cortes. Hay que llamarlo una vez por fotograma. */
    public void sondear() {
        if (fase == Fase.DESCONECTADO) {
            return;
        }
        while (fase != Fase.DESCONECTADO) {
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
            if (!origen.equals(servidor)) {
                continue;
            }
            entrante.flip();
            try {
                procesar(entrante);
            } catch (RuntimeException e) {
                // Paquete mal formado: se ignora.
            }
        }
        avanzar();
    }

    private void procesar(ByteBuffer paquete) {
        switch (Protocolo.leerTipo(paquete)) {
            case Protocolo.BIENVENIDA:
                atenderBienvenida(paquete);
                break;
            case Protocolo.FILA_MAPA:
                atenderFila(paquete);
                break;
            case Protocolo.ESTADO:
                atenderEstado(paquete);
                break;
            case Protocolo.SALA_LLENA:
                if (fase == Fase.CONECTANDO) {
                    desconectar("La sala esta llena");
                }
                break;
            case Protocolo.ADIOS:
                if (bienvenida && paquete.remaining() >= 4 && paquete.getInt() == token) {
                    desconectar("El anfitrion cerro la sala");
                }
                break;
            default:
                break;
        }
    }

    private void atenderBienvenida(ByteBuffer paquete) {
        if (fase != Fase.CONECTANDO || paquete.remaining() < 1 + 4 + 1) {
            return;
        }
        int id = paquete.get();
        int recibido = paquete.getInt();
        int cantidad = paquete.get() & 0xFF;
        if (cantidad < 1 || cantidad > Protocolo.MAX_FILAS) {
            return;
        }
        if (!bienvenida) {
            bienvenida = true;
            token = recibido;
            idJugador = id;
            filas = new String[cantidad];
        } else if (recibido != token || cantidad != filas.length) {
            return;
        }
    }

    private void atenderFila(ByteBuffer paquete) {
        if (fase != Fase.CONECTANDO || !bienvenida || paquete.remaining() < 2) {
            return;
        }
        int indice = paquete.get() & 0xFF;
        int largo = paquete.get() & 0xFF;
        if (indice >= filas.length || largo > paquete.remaining()) {
            return;
        }
        byte[] datos = new byte[largo];
        paquete.get(datos);
        String fila = CodecMapa.decodificarFila(datos, Arena.COLUMNAS);
        if (fila != null) {
            filas[indice] = fila;
        }
    }

    private void atenderEstado(ByteBuffer paquete) {
        if (fase != Fase.JUGANDO || paquete.remaining() < 4 + 2 * CodecEstado.BYTES + CodecEstado.BYTES_PARTIDA) {
            return;
        }
        int secuencia = paquete.getInt();
        if (hayEstado && !Protocolo.esMasNueva(secuencia, ultimaSecuenciaEstado)) {
            return;
        }
        // Se lee primero a objetos de paso: si el segundo estado estuviera mal, no se pisa tampoco el primero.
        EstadoJugador uno = new EstadoJugador();
        EstadoJugador dos = new EstadoJugador();
        EstadoPartida partida = new EstadoPartida();
        if (!CodecEstado.leer(paquete, uno) || !CodecEstado.leer(paquete, dos)
            || !CodecEstado.leer(paquete, partida)) {
            return;
        }
        copiar(uno, estados[0]);
        copiar(dos, estados[1]);
        estadoPartida.fase = partida.fase;
        estadoPartida.numeroRonda = partida.numeroRonda;
        estadoPartida.rondasGanadasUno = partida.rondasGanadasUno;
        estadoPartida.rondasGanadasDos = partida.rondasGanadasDos;
        estadoPartida.ganadorDeLaRonda = partida.ganadorDeLaRonda;
        hayEstado = true;
        ultimaSecuenciaEstado = secuencia;
        ultimoMensaje = reloj.getAsLong();
    }

    private static void copiar(EstadoJugador origen, EstadoJugador destino) {
        destino.x = origen.x;
        destino.y = origen.y;
        destino.vida = origen.vida;
        destino.mirandoDerecha = origen.mirandoDerecha;
        destino.extension = origen.extension;
        destino.vivo = origen.vivo;
        destino.corriendo = origen.corriendo;
        destino.anguloEspada = origen.anguloEspada;
        destino.tiempoRestanteFlash = origen.tiempoRestanteFlash;
        destino.contadorGolpes = origen.contadorGolpes;
        destino.contadorAterrizajes = origen.contadorAterrizajes;
    }

    private void avanzar() {
        long ahora = reloj.getAsLong();
        if (fase == Fase.CONECTANDO) {
            if (bienvenida && mapaCompleto()) {
                fase = Fase.JUGANDO;
                ultimoMensaje = ahora;
                return;
            }
            if (ahora - inicio > Protocolo.TIEMPO_CONEXION_MS) {
                desconectar("No se pudo conectar con la sala");
            } else if (ahora - ultimoHello >= Protocolo.REINTENTO_MS) {
                ultimoHello = ahora;
                enviarHello();
            }
        } else if (fase == Fase.JUGANDO && ahora - ultimoMensaje > Protocolo.TIEMPO_SIN_NOTICIAS_MS) {
            desconectar("Se perdio la conexion con la sala");
        }
    }

    private boolean mapaCompleto() {
        for (String fila : filas) {
            if (fila == null) {
                return false;
            }
        }
        return true;
    }

    /** El saludo lleva una mascara con las filas que faltan: el anfitrion reenvia solo esas. */
    private void enviarHello() {
        long faltantes = -1L;
        if (bienvenida) {
            faltantes = 0L;
            for (int i = 0; i < filas.length; i++) {
                if (filas[i] == null) {
                    faltantes |= 1L << i;
                }
            }
        }
        ByteBuffer hello = Protocolo.nuevo(Protocolo.HELLO);
        hello.putLong(faltantes);
        Protocolo.enviar(canal, hello, servidor);
    }

    private void desconectar(String razon) {
        fase = Fase.DESCONECTADO;
        motivo = razon;
    }

    /**
     * Manda la entrada de este fotograma: teclas, punto apuntado y contadores. Las acciones de una sola
     * vez (saltar, estocar) suben un contador, asi si este paquete se pierde el siguiente las lleva igual.
     */
    public void enviarEntrada(EntradaJugador entrada) {
        if (fase != Fase.JUGANDO) {
            return;
        }
        if (entrada.saltar) {
            contadorSalto++;
        }
        if (entrada.estocada) {
            contadorEstocada++;
        }
        ByteBuffer paquete = Protocolo.nuevo(Protocolo.ENTRADA);
        paquete.putInt(token);
        paquete.putInt(++secuenciaEntrada);
        paquete.put((byte) ((entrada.izquierda ? 1 : 0) | (entrada.derecha ? 2 : 0) | (entrada.hayPuntero ? 4 : 0)));
        paquete.put(contadorSalto);
        paquete.put(contadorEstocada);
        // El punto apuntado va en pixeles del mundo, como entero de 16 bits: la arena mide menos de 4000.
        paquete.putShort(aCorto(entrada.apuntaX));
        paquete.putShort(aCorto(entrada.apuntaY));
        Protocolo.enviar(canal, paquete, servidor);
    }

    private static short aCorto(float valor) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(valor)));
    }

    public Fase getFase() {
        return fase;
    }

    /** Por que se corto la conexion, para mostrarselo al jugador. Vacio mientras no se corto. */
    public String getMotivo() {
        return motivo;
    }

    /** Indice de este jugador en la sala (0 es el anfitrion). */
    public int getIdJugador() {
        return idJugador;
    }

    /** Las filas del mapa. Solo es valido cuando la fase es {@link Fase#JUGANDO}. */
    public String[] getFilas() {
        return filas.clone();
    }

    /** Verdadero cuando llego al menos un estado de la partida. */
    public boolean hayEstado() {
        return hayEstado;
    }

    /** Como va la partida: etapa, ronda, marcador y quien gano la ultima ronda. */
    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }

    /** El ultimo estado recibido del jugador {@code indice} (0 anfitrion, 1 cliente). */
    public EstadoJugador getEstado(int indice) {
        return estados[indice];
    }

    @Override
    public void close() {
        if (bienvenida && fase != Fase.DESCONECTADO) {
            for (int i = 0; i < 2; i++) {
                ByteBuffer adios = Protocolo.nuevo(Protocolo.ADIOS);
                adios.putInt(token);
                Protocolo.enviar(canal, adios, servidor);
            }
        }
        try {
            canal.close();
        } catch (IOException e) {
            // Ya se esta cerrando: no hay nada mas que hacer.
        }
    }
}
