package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.red.ClienteSala;
import com.et35.technomortemarena.red.Protocolo;
import com.et35.technomortemarena.red.ServidorSala;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Pantalla del multijugador: crear una sala (ser el anfitrion) o unirse a la de otro.
 *
 * <p>Se une escribiendo la direccion del anfitrion como {@code ip:puerto} (por ejemplo
 * {@code 127.0.0.1:7777} para probar con dos instancias en la misma maquina). Para crear una sala se
 * usa el puerto de ese mismo campo, o {@value Protocolo#PUERTO_POR_DEFECTO} si no se escribio ninguno.
 *
 * <p>Esta misma pantalla se muestra mientras el cliente conecta y baja el mapa, y cuando se vuelve de
 * una partida con el motivo por el que se cerro la sala.
 */
public class PantallaMultijugador extends ScreenAdapter {

    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color COLOR_CAMPO = new Color(0.14f, 0.15f, 0.21f, 1f);
    private static final Color COLOR_AVISO = new Color(1f, 0.82f, 0.35f, 1f);
    private static final int LARGO_MAXIMO = 60;

    /** La ultima direccion escrita, para no tener que volver a escribirla al reintentar. */
    private static String ultimaDireccion = "127.0.0.1:" + Protocolo.PUERTO_POR_DEFECTO;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    private final Rectangle campoDireccion = new Rectangle(ANCHO_MUNDO / 2f - 200f, 330f, 400f, 44f);
    private final Rectangle botonCrear = new Rectangle(ANCHO_MUNDO / 2f - 150f, 250f, 300f, 50f);
    private final Rectangle botonUnirse = new Rectangle(ANCHO_MUNDO / 2f - 150f, 190f, 300f, 50f);
    private final Rectangle botonVolver = new Rectangle(ANCHO_MUNDO / 2f - 150f, 110f, 300f, 50f);

    private final StringBuilder direccion = new StringBuilder(ultimaDireccion);
    private String aviso;
    /** No es nulo mientras se esta conectando a una sala. */
    private ClienteSala cliente;
    private String destinoTexto = "";

    /** @param aviso mensaje para mostrar al entrar, por ejemplo por que se cerro la sala; puede ser vacio */
    public PantallaMultijugador(RecursosGraficos recursos, RecursosAudio audio, String aviso) {
        this.recursos = recursos;
        this.audio = audio;
        this.aviso = aviso;
    }

    @Override
    public void show() {
        // El campo de texto recibe las letras por el procesador de entrada; el resto de la pantalla
        // sigue leyendo el teclado y el mouse directamente.
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char caracter) {
                escribir(caracter);
                return true;
            }
        });
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    private void escribir(char caracter) {
        if (cliente != null) {
            return;
        }
        if (caracter == '\b') {
            if (direccion.length() > 0) {
                direccion.setLength(direccion.length() - 1);
            }
        } else if (direccion.length() < LARGO_MAXIMO && esCaracterDeDireccion(caracter)) {
            direccion.append(caracter);
        }
    }

    private static boolean esCaracterDeDireccion(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
            || c == '.' || c == ':' || c == '-' || c == '_';
    }

    @Override
    public void render(float delta) {
        if (cliente != null) {
            atenderConexion();
            return;
        }
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreCrear = botonCrear.contains(mouse);
        boolean sobreUnirse = botonUnirse.contains(mouse);
        boolean sobreVolver = botonVolver.contains(mouse);
        boolean toque = Gdx.input.justTouched();

        if (toque && sobreCrear) {
            crearSala();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (toque && sobreUnirse)) {
            unirse();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (toque && sobreVolver)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return;
        }
        dibujar(sobreCrear, sobreUnirse, sobreVolver);
    }

    /** Elige el mapa y abre la sala con el. Si el puerto esta ocupado, vuelve aca con el error. */
    private void crearSala() {
        ultimaDireccion = direccion.toString();
        int puerto = puertoDe(ultimaDireccion);
        if (puerto < 0) {
            aviso = "El puerto no es valido (1 a 65535)";
            return;
        }
        cambiarA(new PantallaSeleccionMapa(recursos, audio,
            arena -> {
                try {
                    ServidorSala sala = ServidorSala.abrir(puerto, arena.getFilas(), Protocolo.RELOJ_SISTEMA);
                    return new PantallaArena(recursos, audio, arena, sala);
                } catch (IOException | RuntimeException e) {
                    return new PantallaMultijugador(recursos, audio,
                        "No se pudo abrir la sala en el puerto " + puerto + " (esta en uso?)");
                }
            },
            () -> new PantallaMultijugador(recursos, audio, "")));
    }

    private void unirse() {
        ultimaDireccion = direccion.toString();
        String texto = ultimaDireccion.trim();
        if (texto.isEmpty()) {
            aviso = "Escribi la direccion del anfitrion";
            return;
        }
        String host = texto;
        int puerto = Protocolo.PUERTO_POR_DEFECTO;
        int dosPuntos = texto.lastIndexOf(':');
        if (dosPuntos >= 0) {
            host = texto.substring(0, dosPuntos);
            puerto = parsearPuerto(texto.substring(dosPuntos + 1));
        }
        if (host.isEmpty() || puerto < 0) {
            aviso = "La direccion debe ser ip:puerto, por ejemplo 127.0.0.1:7777";
            return;
        }
        try {
            cliente = ClienteSala.conectar(new InetSocketAddress(host, puerto), Protocolo.RELOJ_SISTEMA);
            destinoTexto = host + ":" + puerto;
            aviso = "";
        } catch (IOException | RuntimeException e) {
            aviso = "No se pudo usar la direccion " + host;
        }
    }

    /**
     * Puerto de un texto como {@code ip:puerto}, {@code ip} o solo {@code puerto}.
     *
     * @return el puerto, el por defecto si el texto no trae ninguno, o -1 si trae uno que no es valido
     */
    private static int puertoDe(String texto) {
        String limpio = texto.trim();
        int dosPuntos = limpio.lastIndexOf(':');
        if (dosPuntos >= 0) {
            return parsearPuerto(limpio.substring(dosPuntos + 1));
        }
        return soloDigitos(limpio) ? parsearPuerto(limpio) : Protocolo.PUERTO_POR_DEFECTO;
    }

    /** @return el puerto, el por defecto si el texto esta vacio, o -1 si no es un numero entre 1 y 65535 */
    private static int parsearPuerto(String texto) {
        String limpio = texto.trim();
        if (limpio.isEmpty()) {
            return Protocolo.PUERTO_POR_DEFECTO;
        }
        if (!soloDigitos(limpio) || limpio.length() > 5) {
            return -1;
        }
        int puerto = Integer.parseInt(limpio);
        return puerto >= 1 && puerto <= 65535 ? puerto : -1;
    }

    private static boolean soloDigitos(String texto) {
        return !texto.isEmpty() && texto.chars().allMatch(c -> c >= '0' && c <= '9');
    }

    /** Mientras conecta: avanza la conexion y pasa a la partida cuando el mapa esta completo. */
    private void atenderConexion() {
        cliente.sondear();
        if (cliente.getFase() == ClienteSala.Fase.JUGANDO) {
            ClienteSala conectado = cliente;
            cliente = null;
            cambiarA(new PantallaArena(recursos, audio, conectado));
            return;
        }
        if (cliente.getFase() == ClienteSala.Fase.DESCONECTADO) {
            aviso = cliente.getMotivo();
            cliente.close();
            cliente = null;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
            || (Gdx.input.justTouched() && botonVolver.contains(viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY()))))) {
            cliente.close();
            cliente = null;
            aviso = "";
            return;
        }
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        centrado("CONECTANDO...", 330f, 2f, Color.WHITE);
        centrado(destinoTexto, 290f, 1.2f, Color.LIGHT_GRAY);
        ui.boton(batch, botonVolver, botonVolver.contains(viewport.unproject(
            new Vector2(Gdx.input.getX(), Gdx.input.getY()))), "CANCELAR", 1.3f);
        batch.end();
    }

    private void centrado(String texto, float y, float escala, Color color) {
        float x = (ANCHO_MUNDO - ui.anchoTexto(texto, escala)) / 2f;
        ui.texto(batch, texto, x, y, escala, color);
    }

    private void dibujar(boolean sobreCrear, boolean sobreUnirse, boolean sobreVolver) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        centrado("MULTIJUGADOR", ALTO_MUNDO - 90f, 2.5f, Color.WHITE);
        ui.texto(batch, "DIRECCION DEL ANFITRION (IP:PUERTO)", campoDireccion.x, campoDireccion.y + 66f, 1.1f, Color.LIGHT_GRAY);
        ui.rectangulo(batch, campoDireccion, COLOR_CAMPO);
        String mostrado = direccion + ((int) (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        ui.texto(batch, mostrado, campoDireccion.x + 12f, campoDireccion.y + 30f, 1.3f, Color.WHITE);
        ui.boton(batch, botonCrear, sobreCrear, "CREAR SALA", 1.3f);
        ui.boton(batch, botonUnirse, sobreUnirse, "UNIRSE A UNA SALA", 1.3f);
        ui.boton(batch, botonVolver, sobreVolver, "VOLVER", 1.3f);
        if (!aviso.isEmpty()) {
            centrado(aviso, 80f, 1.1f, COLOR_AVISO);
        }
        batch.end();
    }

    /** Igual que en las demas pantallas: {@code setScreen} no libera la anterior, hay que disponerla a mano. */
    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (cliente != null) {
            cliente.close();
            cliente = null;
        }
        batch.dispose();
        ui.dispose();
    }
}
