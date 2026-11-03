package com.et35.technomortemarena.red;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.ColocadorPiezas;
import com.et35.technomortemarena.partida.EdicionNivel;
import com.et35.technomortemarena.partida.EstadoPartida;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la sala de punta a punta con sockets UDP reales sobre loopback. El reloj es falso: el
 * tiempo solo avanza cuando el test lo dice, asi los timeouts se prueban sin esperar.
 */
class SalaLoopbackTest {

    private final AtomicLong reloj = new AtomicLong();
    private final String[] filas = Arena.conCintasYPinchos().getFilas();
    private ServidorSala servidor;
    private ClienteSala cliente;
    private ClienteSala otroCliente;
    private DatagramChannel crudo;

    @BeforeEach
    void abrirSala() throws IOException {
        servidor = ServidorSala.abrir(0, filas, reloj::get);
    }

    @AfterEach
    void cerrarTodo() throws IOException {
        if (cliente != null) {
            cliente.close();
        }
        if (otroCliente != null) {
            otroCliente.close();
        }
        if (crudo != null) {
            crudo.close();
        }
        servidor.close();
    }

    private InetSocketAddress direccionDeLaSala() {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), servidor.getPuerto());
    }

    private ClienteSala nuevoCliente() throws IOException {
        return ClienteSala.conectar(direccionDeLaSala(), reloj::get);
    }

    /** Hace girar a los dos lados hasta que se cumpla la condicion, avanzando el reloj falso. */
    private void girarHasta(BooleanSupplier condicion) throws InterruptedException {
        for (int i = 0; i < 400 && !condicion.getAsBoolean(); i++) {
            reloj.addAndGet(20);
            servidor.sondear();
            if (cliente != null) {
                cliente.sondear();
            }
            if (otroCliente != null) {
                otroCliente.sondear();
            }
            Thread.sleep(2);
        }
        assertTrue(condicion.getAsBoolean(), "la condicion no se cumplio a tiempo");
    }

    private void conectarCliente() throws IOException, InterruptedException {
        cliente = nuevoCliente();
        girarHasta(() -> cliente.getFase() == ClienteSala.Fase.JUGANDO);
    }

    @Test
    void elClienteBajaElMapaIgualAlDelAnfitrion() throws Exception {
        conectarCliente();

        assertArrayEquals(filas, cliente.getFilas());
        assertEquals(1, cliente.getIdJugador());
        assertTrue(servidor.hayCliente());
    }

    @Test
    void elMapaBajadoArmaLaMismaArena() throws Exception {
        conectarCliente();

        Arena local = Arena.desdeGrilla(filas);
        Arena remota = Arena.desdeGrilla(cliente.getFilas());

        assertEquals(local.getBloques().size(), remota.getBloques().size());
    }

    @Test
    void elClienteEligeYColocaUnObjetoYAmbosQuedanConElMismoMapa() throws Exception {
        conectarCliente();
        EdicionNivel edicion = new EdicionNivel(filas, new ArrayList<>(), new Random(7));
        edicion.iniciar(1); // pierde el cliente: edita el Jugador 2

        // el cliente elige la segunda tarjeta, apunta a una celda del aire y pide colocar
        EntradaJugador mano = new EntradaJugador();
        mano.hayPuntero = true;
        mano.apuntaX = 50 * Arena.CELDA;
        mano.apuntaY = 20 * Arena.CELDA;
        mano.eleccionPieza = 1;
        mano.colocarPieza = true;
        cliente.enviarEntrada(mano);
        EntradaJugador recibida = new EntradaJugador();
        // leer() consume las acciones de una sola vez: se guarda lo que llego en la primera lectura que las trae
        EntradaJugador conLaAccion = new EntradaJugador();
        girarHasta(() -> {
            servidor.getEntradaRemota().leer(recibida);
            if (recibida.colocarPieza) {
                conLaAccion.eleccionPieza = recibida.eleccionPieza;
                conLaAccion.apuntaX = recibida.apuntaX;
                conLaAccion.apuntaY = recibida.apuntaY;
                conLaAccion.colocarPieza = true;
            }
            return conLaAccion.colocarPieza;
        });
        recibida.eleccionPieza = conLaAccion.eleccionPieza;
        recibida.apuntaX = conLaAccion.apuntaX;
        recibida.apuntaY = conLaAccion.apuntaY;

        assertEquals(1, recibida.eleccionPieza);
        assertEquals(50 * Arena.CELDA, recibida.apuntaX, 1f);
        edicion.moverCursor(ColocadorPiezas.columnaDe(recibida.apuntaX), ColocadorPiezas.filaDe(recibida.apuntaY));
        edicion.elegir(recibida.eleccionPieza);
        assertTrue(edicion.colocar());
        edicion.terminar();

        // el anfitrion manda el estado con la lista de piezas y el cliente rearma el mismo mapa
        EstadoPartida estado = new EstadoPartida();
        edicion.exportar(estado);
        girarHasta(() -> {
            servidor.enviarEstado(new EstadoJugador(), new EstadoJugador(), estado);
            return cliente.hayEstado() && cliente.getEstadoPartida().piezas.size() == 1;
        });

        assertArrayEquals(edicion.getFilas(), EdicionNivel.reconstruir(cliente.getFilas(), cliente.getEstadoPartida().piezas));
        // y el cliente no vuelve a disparar la colocacion: es una accion de una sola vez
        cliente.enviarEntrada(new EntradaJugador());
        girarHasta(() -> {
            servidor.getEntradaRemota().leer(recibida);
            return recibida.eleccionPieza == -1;
        });
        servidor.getEntradaRemota().leer(recibida);
        assertFalse(recibida.colocarPieza);
    }

    @Test
    void laEntradaDelClienteLlegaAlAnfitrion() throws Exception {
        conectarCliente();
        EntradaJugador teclas = new EntradaJugador();
        teclas.izquierda = true;
        teclas.saltar = true;
        teclas.estocada = true;
        teclas.hayPuntero = true;
        teclas.apuntaX = 1234.4f;
        teclas.apuntaY = 567.6f;

        cliente.enviarEntrada(teclas);
        girarHasta(servidor::clienteListo);

        EntradaJugador recibida = new EntradaJugador();
        servidor.getEntradaRemota().leer(recibida);
        assertTrue(recibida.izquierda);
        assertTrue(recibida.saltar);
        assertTrue(recibida.estocada);
        assertTrue(recibida.hayPuntero);
        assertEquals(1234f, recibida.apuntaX, "el punto apuntado viaja redondeado a pixeles");
        assertEquals(568f, recibida.apuntaY);
        servidor.getEntradaRemota().leer(recibida);
        assertFalse(recibida.saltar, "el salto solo vale una vez");
        assertFalse(recibida.estocada, "la estocada solo vale una vez");
        assertEquals(1234f, recibida.apuntaX, "el punto apuntado se mantiene");
    }

    @Test
    void elEstadoDelAnfitrionLlegaAlCliente() throws Exception {
        conectarCliente();
        cliente.enviarEntrada(new EntradaJugador());
        girarHasta(servidor::clienteListo);
        EstadoJugador anfitrion = new EstadoJugador();
        anfitrion.x = 321.5f;
        anfitrion.vida = 3;
        anfitrion.vivo = true;
        EstadoJugador remoto = new EstadoJugador();
        remoto.x = 99f;
        remoto.vida = 2;
        remoto.vivo = true;

        EstadoPartida partida = new EstadoPartida();
        partida.fase = 1;
        partida.numeroRonda = 3;
        partida.rondasGanadasUno = 2;
        partida.rondasGanadasDos = 1;
        partida.ganadorDeLaRonda = 0;

        servidor.enviarEstado(anfitrion, remoto, partida);
        girarHasta(cliente::hayEstado);

        assertEquals(321.5f, cliente.getEstado(0).x);
        assertEquals(99f, cliente.getEstado(1).x);
        assertEquals(2, cliente.getEstado(1).vida);
        assertEquals(1, cliente.getEstadoPartida().fase);
        assertEquals(3, cliente.getEstadoPartida().numeroRonda);
        assertEquals(2, cliente.getEstadoPartida().rondasGanadasUno);
        assertEquals(1, cliente.getEstadoPartida().rondasGanadasDos);
        assertEquals(0, cliente.getEstadoPartida().ganadorDeLaRonda);
    }

    @Test
    void unEstadoViejoNoPisaAlNuevo() throws Exception {
        conectarCliente();
        cliente.enviarEntrada(new EntradaJugador());
        girarHasta(servidor::clienteListo);
        EstadoJugador a = new EstadoJugador();
        a.vida = 1;

        EstadoPartida partida = new EstadoPartida();
        a.x = 1f;
        servidor.enviarEstado(a, a, partida);
        a.x = 2f;
        servidor.enviarEstado(a, a, partida);
        girarHasta(() -> cliente.hayEstado() && cliente.getEstado(0).x == 2f);

        assertEquals(2f, cliente.getEstado(0).x);
    }

    @Test
    void unSegundoClienteEncuentraLaSalaLlena() throws Exception {
        conectarCliente();
        otroCliente = nuevoCliente();

        girarHasta(() -> otroCliente.getFase() == ClienteSala.Fase.DESCONECTADO);

        assertEquals("La sala esta llena", otroCliente.getMotivo());
        assertEquals(ClienteSala.Fase.JUGANDO, cliente.getFase(), "el primer cliente no se ve afectado");
    }

    @Test
    void sinSalaElClienteDesisteDespuesDelTiempoDeConexion() throws Exception {
        int puertoLibre;
        try (DatagramChannel temporal = DatagramChannel.open()) {
            temporal.bind(new InetSocketAddress(0));
            puertoLibre = ((InetSocketAddress) temporal.getLocalAddress()).getPort();
        }
        cliente = ClienteSala.conectar(new InetSocketAddress(InetAddress.getLoopbackAddress(), puertoLibre), reloj::get);

        girarHasta(() -> cliente.getFase() == ClienteSala.Fase.DESCONECTADO);

        assertEquals("No se pudo conectar con la sala", cliente.getMotivo());
        assertTrue(reloj.get() >= Protocolo.TIEMPO_CONEXION_MS);
    }

    @Test
    void unaDireccionQueNoExisteFallaAlConectar() {
        InetSocketAddress sinResolver = InetSocketAddress.createUnresolved("no-existe.invalid", 7777);

        assertThrows(IOException.class, () -> ClienteSala.conectar(sinResolver, reloj::get));
    }

    @Test
    void elAnfitrionSueltaAlClienteQueDejaDeMandar() throws Exception {
        conectarCliente();
        cliente.enviarEntrada(new EntradaJugador());
        girarHasta(servidor::clienteListo);

        reloj.addAndGet(Protocolo.TIEMPO_SIN_NOTICIAS_MS + 1);
        servidor.sondear();

        assertFalse(servidor.hayCliente());
        assertTrue(servidor.consumirDesconexion());
        assertFalse(servidor.consumirDesconexion(), "el aviso se da una sola vez");
    }

    @Test
    void elClienteSeDaCuentaSiElAnfitrionDejaDeResponder() throws Exception {
        conectarCliente();

        reloj.addAndGet(Protocolo.TIEMPO_SIN_NOTICIAS_MS + 1);
        cliente.sondear();

        assertEquals(ClienteSala.Fase.DESCONECTADO, cliente.getFase());
        assertEquals("Se perdio la conexion con la sala", cliente.getMotivo());
    }

    @Test
    void siElClienteSeVaElAnfitrionLoSabe() throws Exception {
        conectarCliente();
        cliente.enviarEntrada(new EntradaJugador());
        girarHasta(servidor::clienteListo);

        cliente.close();
        cliente = null;
        girarHasta(() -> !servidor.hayCliente());

        assertTrue(servidor.consumirDesconexion());
    }

    @Test
    void siElAnfitrionCierraElClienteSeEntera() throws Exception {
        conectarCliente();

        servidor.close();
        girarHasta(() -> cliente.getFase() == ClienteSala.Fase.DESCONECTADO);

        assertEquals("El anfitrion cerro la sala", cliente.getMotivo());
        servidor = ServidorSala.abrir(0, filas, reloj::get);
    }

    @Test
    void despuesDeSalirUnClienteLaSalaAceptaOtro() throws Exception {
        conectarCliente();
        cliente.enviarEntrada(new EntradaJugador());
        girarHasta(servidor::clienteListo);
        cliente.close();
        cliente = null;
        girarHasta(() -> !servidor.hayCliente());

        otroCliente = nuevoCliente();
        girarHasta(() -> otroCliente.getFase() == ClienteSala.Fase.JUGANDO);

        assertArrayEquals(filas, otroCliente.getFilas());
    }

    private void mandarCrudo(ByteBuffer paquete) throws IOException {
        if (crudo == null) {
            crudo = DatagramChannel.open();
            crudo.configureBlocking(false);
        }
        paquete.flip();
        crudo.send(paquete, direccionDeLaSala());
    }

    /** Un "cliente" hecho a mano: se une a la sala y devuelve el token que le dio el anfitrion. */
    private int unirseACrudo(DatagramChannel canal) throws Exception {
        ByteBuffer hello = Protocolo.nuevo(Protocolo.HELLO);
        hello.putLong(0L);
        hello.flip();
        canal.send(hello, direccionDeLaSala());
        Thread.sleep(30);
        servidor.sondear();
        ByteBuffer recibido = ByteBuffer.allocate(Protocolo.MAX_DATAGRAMA);
        for (int i = 0; i < 50; i++) {
            recibido.clear();
            if (canal.receive(recibido) != null) {
                recibido.flip();
                if (Protocolo.leerTipo(recibido) == Protocolo.BIENVENIDA) {
                    recibido.get();
                    return recibido.getInt();
                }
            } else {
                Thread.sleep(10);
            }
        }
        throw new AssertionError("el anfitrion no respondio la bienvenida");
    }

    private ByteBuffer entradaCruda(int token, int secuencia, int banderas) {
        ByteBuffer paquete = Protocolo.nuevo(Protocolo.ENTRADA);
        paquete.putInt(token);
        paquete.putInt(secuencia);
        paquete.put((byte) banderas);
        paquete.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) -1).put((byte) 0);
        paquete.putShort((short) 0).putShort((short) 0);
        paquete.flip();
        return paquete;
    }

    @Test
    void unaEntradaConTokenEquivocadoSeIgnora() throws Exception {
        try (DatagramChannel falso = DatagramChannel.open()) {
            falso.configureBlocking(false);
            int token = unirseACrudo(falso);

            falso.send(entradaCruda(token + 1, 1, 2), direccionDeLaSala());
            Thread.sleep(30);
            servidor.sondear();
            assertFalse(servidor.clienteListo(), "con el token equivocado no se acepta la entrada");

            falso.send(entradaCruda(token, 2, 2), direccionDeLaSala());
            Thread.sleep(30);
            servidor.sondear();
            assertTrue(servidor.clienteListo(), "con el token correcto si");
            EntradaJugador salida = new EntradaJugador();
            servidor.getEntradaRemota().leer(salida);
            assertTrue(salida.derecha);
        }
    }

    @Test
    void unTerceroConElTokenCorrectoPeroOtraDireccionNoPuedeManejarAlJugador() throws Exception {
        try (DatagramChannel legitimo = DatagramChannel.open(); DatagramChannel intruso = DatagramChannel.open()) {
            legitimo.configureBlocking(false);
            int token = unirseACrudo(legitimo);

            intruso.send(entradaCruda(token, 1, 2), direccionDeLaSala());
            Thread.sleep(30);
            servidor.sondear();
            assertFalse(servidor.clienteListo(), "la entrada vino de otra direccion");

            legitimo.send(entradaCruda(token, 2, 1), direccionDeLaSala());
            Thread.sleep(30);
            servidor.sondear();
            assertTrue(servidor.clienteListo());
            EntradaJugador salida = new EntradaJugador();
            servidor.getEntradaRemota().leer(salida);
            assertTrue(salida.izquierda);
            assertFalse(salida.derecha, "el intruso no movio al jugador");
        }
    }

    @Test
    void elAnfitrionRespondeSoloConLasFilasQueFaltan() throws Exception {
        ByteBuffer hello = Protocolo.nuevo(Protocolo.HELLO);
        hello.putLong(1L << 2);
        mandarCrudo(hello);
        Thread.sleep(30);
        servidor.sondear();

        int bienvenidas = 0;
        int filasRecibidas = 0;
        ByteBuffer recibido = ByteBuffer.allocate(Protocolo.MAX_DATAGRAMA);
        for (int i = 0; i < 20; i++) {
            recibido.clear();
            if (crudo.receive(recibido) == null) {
                Thread.sleep(10);
                continue;
            }
            recibido.flip();
            int tipo = Protocolo.leerTipo(recibido);
            if (tipo == Protocolo.BIENVENIDA) {
                bienvenidas++;
            } else if (tipo == Protocolo.FILA_MAPA) {
                filasRecibidas++;
                assertEquals(2, recibido.get(), "la unica fila pedida era la 2");
            }
        }

        assertEquals(1, bienvenidas);
        assertEquals(1, filasRecibidas);
    }

    @Test
    void laBasuraNoTumbaNiAlAnfitrionNiAlCliente() throws Exception {
        conectarCliente();
        Random azar = new Random(42);
        for (int i = 0; i < 300; i++) {
            byte[] basura = new byte[azar.nextInt(200)];
            azar.nextBytes(basura);
            // la mitad con la cabecera correcta, para llegar mas adentro del procesamiento
            if (i % 2 == 0 && basura.length >= 4) {
                basura[0] = 0x54;
                basura[1] = 0x4D;
                basura[2] = Protocolo.VERSION;
                basura[3] = (byte) (1 + azar.nextInt(7));
            }
            ByteBuffer paquete = ByteBuffer.allocate(Math.max(basura.length, 1));
            paquete.put(basura);
            mandarCrudo(paquete);
        }
        // y los mismos bytes dirigidos al cliente
        DatagramChannel haciaCliente = crudo;
        Thread.sleep(30);
        servidor.sondear();
        cliente.sondear();

        assertNotNull(haciaCliente);
        EntradaJugador teclas = new EntradaJugador();
        teclas.derecha = true;
        cliente.enviarEntrada(teclas);
        girarHasta(servidor::clienteListo);
        EntradaJugador recibida = new EntradaJugador();
        servidor.getEntradaRemota().leer(recibida);
        assertTrue(recibida.derecha, "la sala siguio funcionando despues de la basura");
    }
}
