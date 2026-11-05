package com.et35.technomortemarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * Punto unico de carga y liberacion de los efectos de sonido del juego.
 *
 * <p>Igual que {@link RecursosGraficos}, centralizar los efectos evita el error de cargarlos dentro
 * del bucle de dibujo: crear un {@link Sound} en cada fotograma abriria el archivo una y otra vez y
 * reservaria memoria que el recolector de basura de Java no administra. Los sonidos se cargan una
 * sola vez y se reutilizan en todas las rondas.
 *
 * <p>Se guardan como {@link Sound} y no como {@code Music} porque son efectos cortos que conviene
 * tener decodificados en memoria: asi se pueden superponer y repetir en bucle (el de correr) sin
 * cortes ni pausas al leer del disco. La musica del menu, en cambio, es larga: va como {@link Music}, que
 * la lee del disco de a poco.
 *
 * <p>Las {@link OpcionesAudio} deciden si suenan la musica y los efectos: los efectos se reproducen con
 * {@link #reproducir} y {@link #repetir}, que no hacen nada si estan apagados.
 */
public class RecursosAudio implements Disposable {

    /** Pisada corta que suena en bucle mientras un jugador corre. */
    public final Sound correr;

    /** Golpe de la espada contra el cuerpo del rival. */
    public final Sound golpe;

    /** Aterrizaje despues de un salto o una caida. */
    public final Sound aterrizar;

    /** Sonido de interfaz al pasar el cursor por encima de un boton. */
    public final Sound boton;

    /** Sonido de interfaz al hacer clic en un boton. */
    public final Sound botonSeleccionado;

    /** Volumen de la musica del menu: queda por debajo de los sonidos de los botones. */
    private static final float VOLUMEN_MUSICA = 0.5f;

    private final Music musicaMenu;
    private final OpcionesAudio opciones;
    /** Si la pantalla actual quiere la musica del menu (las del menu si, la partida y el editor no). */
    private boolean pideMusicaMenu;

    public RecursosAudio() {
        correr = cargar("sounds/Running.mp3");
        golpe = cargar("sounds/Bloody Sword Hit Sound Effect.mp3");
        aterrizar = cargar("sounds/Landing.wav");
        boton = cargar("sounds/Boton.wav");
        botonSeleccionado = cargar("sounds/Boton Seleccionado.mp3");
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("music/menu.mp3"));
        musicaMenu.setLooping(true);
        musicaMenu.setVolume(VOLUMEN_MUSICA);
        Preferences preferencias = Gdx.app.getPreferences("techno-mortem-arena");
        opciones = new OpcionesAudio(new OpcionesAudio.Almacen() {
            @Override
            public boolean leer(String clave, boolean porDefecto) {
                return preferencias.getBoolean(clave, porDefecto);
            }

            @Override
            public void guardar(String clave, boolean valor) {
                preferencias.putBoolean(clave, valor);
                preferencias.flush();
            }
        });
    }

    public OpcionesAudio getOpciones() {
        return opciones;
    }

    /** Reproduce un efecto una vez, si los efectos estan prendidos. */
    public void reproducir(Sound sonido, float volumen) {
        if (opciones.isEfectos()) {
            sonido.play(volumen);
        }
    }

    /** Reproduce un efecto en bucle, si los efectos estan prendidos. Se corta con {@code sonido.stop()}. */
    public void repetir(Sound sonido, float volumen) {
        if (opciones.isEfectos()) {
            sonido.loop(volumen);
        }
    }

    /**
     * Cada pantalla dice si quiere la musica del menu: las del menu la piden y la partida y el editor no. Si
     * ya estaba sonando sigue sin cortarse, asi no vuelve a empezar al pasar de una pantalla del menu a otra.
     */
    public void pedirMusicaMenu(boolean pide) {
        pideMusicaMenu = pide;
        actualizarMusica();
    }

    /** Prende o apaga la musica y la deja sonando o callada al momento. */
    public void setMusica(boolean musica) {
        opciones.setMusica(musica);
        actualizarMusica();
    }

    public void setEfectos(boolean efectos) {
        opciones.setEfectos(efectos);
    }

    private void actualizarMusica() {
        boolean debeSonar = pideMusicaMenu && opciones.isMusica();
        if (debeSonar && !musicaMenu.isPlaying()) {
            musicaMenu.play();
        } else if (!debeSonar && musicaMenu.isPlaying()) {
            musicaMenu.stop();
        }
    }

    /**
     * Carga un efecto desde la carpeta {@code assets/} del proyecto.
     *
     * <p>La ruta es relativa a {@code assets/} y usa barras normales. {@code Gdx.files.internal} busca
     * primero en el directorio de trabajo y despues en el classpath, por lo que funciona tanto al
     * ejecutar desde el IDE como desde el .jar empaquetado.
     */
    private static Sound cargar(String ruta) {
        return Gdx.audio.newSound(Gdx.files.internal(ruta));
    }

    @Override
    public void dispose() {
        correr.dispose();
        golpe.dispose();
        aterrizar.dispose();
        boton.dispose();
        botonSeleccionado.dispose();
        musicaMenu.dispose();
    }
}
