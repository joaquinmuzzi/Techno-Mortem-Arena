package com.et35.technomortemarena;

import com.badlogic.gdx.Gdx;
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
 * cortes ni pausas al leer del disco.
 */
public class RecursosAudio implements Disposable {

    /** Pisada que suena en bucle mientras un jugador corre sobre la rejilla metalica. */
    public final Sound correr;

    /** Golpe de la espada contra el cuerpo del rival. */
    public final Sound golpe;

    /** Aterrizaje despues de un salto o una caida. */
    public final Sound aterrizar;

    /** Sonido de interfaz al pasar el cursor por encima de un boton. */
    public final Sound boton;

    /** Sonido de interfaz al hacer clic en un boton. */
    public final Sound botonSeleccionado;

    public RecursosAudio() {
        correr = cargar("sounds/Running On Metal Grate.mp3");
        golpe = cargar("sounds/Bloody Sword Hit Sound Effect.mp3");
        aterrizar = cargar("sounds/Landing-Jump.mp3");
        boton = cargar("sounds/Boton.wav");
        botonSeleccionado = cargar("sounds/Boton Seleccionado.mp3");
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
    }
}
