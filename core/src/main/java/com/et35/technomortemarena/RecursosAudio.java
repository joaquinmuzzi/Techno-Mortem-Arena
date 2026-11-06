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
 * cortes ni pausas al leer del disco. La musica (la del menu y la de combate), en cambio, es larga: va como {@link Music}, que
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

    /** Disparo de la pistola. */
    public final Sound pistolazo;

    /** Disparo de la escopeta. */
    public final Sound escopetazo;

    /** Espada que se recoge del suelo. */
    public final Sound espadaEquipada;

    /** Espada tirada que toca el piso. */
    public final Sound espadaCae;

    /** Pistola o escopeta que se recoge del suelo. */
    public final Sound armaEquipada;

    /** Disparo del lanzacohetes. */
    public final Sound bazucazo;

    /** Explosion de un cohete al chocar. */
    public final Sound explosion;

    /** Disparo del rifle laser. */
    public final Sound disparoLaser;

    /** Arma de fuego tirada que toca el piso. */
    public final Sound armaCae;

    /** Mejora que recoge un jugador (velocidad, salto o punteria). */
    public final Sound powerUp;

    /** Sonido de interfaz al pasar el cursor por encima de un boton. */
    public final Sound boton;

    /** Sonido de interfaz al hacer clic en un boton. */
    public final Sound botonSeleccionado;

    /** Que musica pide cada pantalla: la del menu en el menu, la de combate en la partida y la del editor en el editor. */
    public enum Musica { NINGUNA, MENU, COMBATE, EDITOR }

    /** Volumen de la musica: queda por debajo de los sonidos de los botones y de los golpes. */
    private static final float VOLUMEN_MUSICA = 0.5f;

    private final Music musicaMenu;
    private final Music musicaCombate;
    private final Music musicaEditor;
    private final OpcionesAudio opciones;
    /** La musica que pide la pantalla actual. */
    private Musica pedida = Musica.NINGUNA;

    public RecursosAudio() {
        correr = cargar("sounds/Running.mp3");
        golpe = cargar("sounds/Bloody Sword Hit Sound Effect.mp3");
        aterrizar = cargar("sounds/Landing.wav");
        pistolazo = cargar("sounds/pistol-shot.mp3");
        escopetazo = cargar("sounds/shotgun-blast.mp3");
        espadaEquipada = cargar("sounds/sword-equipped.mp3");
        espadaCae = cargar("sounds/sword-falling.mp3");
        armaEquipada = cargar("sounds/Weapon-Equip.mp3");
        bazucazo = cargar("sounds/bazooka-shot.mp3");
        explosion = cargar("sounds/explosion.mp3");
        disparoLaser = cargar("sounds/disparo-laser.mp3");
        armaCae = cargar("sounds/Gun-drop.mp3");
        powerUp = cargar("sounds/power-up.mp3");
        boton = cargar("sounds/Boton.wav");
        botonSeleccionado = cargar("sounds/Boton Seleccionado.mp3");
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("music/menu.mp3"));
        musicaMenu.setLooping(true);
        musicaMenu.setVolume(VOLUMEN_MUSICA);
        musicaCombate = Gdx.audio.newMusic(Gdx.files.internal("music/combate.mp3"));
        musicaCombate.setLooping(true);
        musicaCombate.setVolume(VOLUMEN_MUSICA);
        musicaEditor = Gdx.audio.newMusic(Gdx.files.internal("music/editor.mp3"));
        musicaEditor.setLooping(true);
        musicaEditor.setVolume(VOLUMEN_MUSICA);
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
     * Cada pantalla dice que musica quiere: las del menu la del menu, la partida la de combate y el editor
     * la suya. Si ya estaba sonando sigue sin cortarse, asi no vuelve a empezar al pasar de una pantalla del
     * menu a otra, ni al reiniciar la partida.
     */
    public void pedirMusica(Musica musica) {
        pedida = musica;
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
        sonarSolo(musicaMenu, pedida == Musica.MENU && opciones.isMusica());
        sonarSolo(musicaCombate, pedida == Musica.COMBATE && opciones.isMusica());
        sonarSolo(musicaEditor, pedida == Musica.EDITOR && opciones.isMusica());
    }

    private static void sonarSolo(Music musica, boolean debeSonar) {
        if (debeSonar && !musica.isPlaying()) {
            musica.play();
        } else if (!debeSonar && musica.isPlaying()) {
            musica.stop();
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
        pistolazo.dispose();
        escopetazo.dispose();
        espadaEquipada.dispose();
        espadaCae.dispose();
        armaEquipada.dispose();
        bazucazo.dispose();
        explosion.dispose();
        disparoLaser.dispose();
        armaCae.dispose();
        powerUp.dispose();
        boton.dispose();
        botonSeleccionado.dispose();
        musicaMenu.dispose();
        musicaCombate.dispose();
        musicaEditor.dispose();
    }
}
