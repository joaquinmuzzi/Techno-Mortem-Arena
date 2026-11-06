package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.entidades.EstadoArmas;
import com.et35.technomortemarena.partida.EstadoPartida;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.LongSupplier;

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
    private final EstadoArmas estadoArmas = new EstadoArmas();
    private boolean hayEstado;
    private int ultimaSecuenciaEstado;

    private int secuenciaEntrada;
    private byte contadorSalto;
    private byte contadorEstocada;
    private byte contadorRodar;
    private byte contadorLanzar;
    private byte contadorColocar;

    private ClienteSala(DatagramChannel canal, SocketAddress servidor, LongSupplier reloj) {
        this.canal = canal;
        this.servidor = servidor;
        this.reloj = reloj;
        this.inicio = reloj.getAsLong();
    }

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
        if (fase != Fase.CONECTANDO || !bienvenida || paquete.remaining() < 3) {
            return;
        }
        int indice = paquete.get() & 0xFF;
        int largo = paquete.getShort() & 0xFFFF;
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
        if (fase != Fase.JUGANDO || paquete.remaining() < 4 + 2 * CodecEstado.BYTES + CodecEstado.BYTES_PARTIDA + CodecEstado.BYTES_ARMAS) {
            return;
        }
        int secuencia = paquete.getInt();
        if (hayEstado && !Protocolo.esMasNueva(secuencia, ultimaSecuenciaEstado)) {
            return;
        }
        EstadoJugador uno = new EstadoJugador();
        EstadoJugador dos = new EstadoJugador();
        EstadoPartida partida = new EstadoPartida();
        EstadoArmas armas = new EstadoArmas();
        if (!CodecEstado.leer(paquete, uno) || !CodecEstado.leer(paquete, dos)
            || !CodecEstado.leer(paquete, partida) || !CodecEstado.leer(paquete, armas)) {
            return;
        }
        copiar(uno, estados[0]);
        copiar(dos, estados[1]);
        estadoPartida.copiarDe(partida);
        estadoArmas.copiarDe(armas);
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
        destino.rodando = origen.rodando;
        destino.arma = origen.arma;
        destino.balas = origen.balas;
        System.arraycopy(origen.tiempoPowerUp, 0, destino.tiempoPowerUp, 0, origen.tiempoPowerUp.length);
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

    private void enviarHello() {
        long faltantesBajas = -1L;
        long faltantesAltas = -1L;
        if (bienvenida) {
            faltantesBajas = 0L;
            faltantesAltas = 0L;
            for (int i = 0; i < filas.length; i++) {
                if (filas[i] == null) {
                    if (i < 64) {
                        faltantesBajas |= 1L << i;
                    } else {
                        faltantesAltas |= 1L << (i - 64);
                    }
                }
            }
        }
        ByteBuffer hello = Protocolo.nuevo(Protocolo.HELLO);
        hello.putLong(faltantesBajas);
        hello.putLong(faltantesAltas);
        Protocolo.enviar(canal, hello, servidor);
    }

    private void desconectar(String razon) {
        fase = Fase.DESCONECTADO;
        motivo = razon;
    }

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
        if (entrada.rodar) {
            contadorRodar++;
        }
        if (entrada.lanzar) {
            contadorLanzar++;
        }
        if (entrada.colocarPieza) {
            contadorColocar++;
        }
        ByteBuffer paquete = Protocolo.nuevo(Protocolo.ENTRADA);
        paquete.putInt(token);
        paquete.putInt(++secuenciaEntrada);
        paquete.put((byte) ((entrada.izquierda ? 1 : 0) | (entrada.derecha ? 2 : 0) | (entrada.hayPuntero ? 4 : 0)));
        paquete.put(contadorSalto);
        paquete.put(contadorEstocada);
        paquete.put(contadorRodar);
        paquete.put(contadorLanzar);
        paquete.put((byte) entrada.eleccionPieza);
        paquete.put(contadorColocar);
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

    public String getMotivo() {
        return motivo;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public String[] getFilas() {
        return filas.clone();
    }

    public boolean hayEstado() {
        return hayEstado;
    }

    public EstadoArmas getEstadoArmas() {
        return estadoArmas;
    }

    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }

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
        }
    }
}
