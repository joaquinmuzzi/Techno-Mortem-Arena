package com.et35.technomortemarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

/**
 * Punto unico de carga y liberacion de las texturas del juego.
 *
 * <p>Centralizar los assets en una sola clase evita el error mas comun en LibGDX: crear una
 * {@link Texture} dentro de {@code render()}. Eso construiria una textura nueva en cada uno de los
 * 60 fotogramas por segundo y nunca liberaria las anteriores, agotando la memoria de video.
 *
 * <p>Toda textura que se crea debe liberarse: LibGDX reserva memoria en la placa de video, que el
 * recolector de basura de Java no administra. Por eso esta clase implementa {@link Disposable}.
 */
public class RecursosGraficos implements Disposable {

    /** Silueta del esgrimista, 48x96 px, mirando a la derecha. */
    public final Texture jugador;

    /** Hoja horizontal, 56x10 px, con la guarda a la izquierda y la punta a la derecha. */
    public final Texture espada;

    /** Antebrazo que sostiene la espada, 24x14 px, mas angosto hacia la muneca. Placeholder. */
    public final Texture brazo;

    /** Baldosa del piso, 64x64 px, disenada para repetirse en horizontal. */
    public final Texture piso;

    /** Pistola, 20x9 px, con la boca a la derecha. */
    public final Texture pistola;

    /** Escopeta, 32x9 px, con la culata de madera a la izquierda y el cano a la derecha. */
    public final Texture escopeta;

    /** Halo amarillo suave, 48x20 px con transparencia: marca las armas que se pueden agarrar. */
    public final Texture brillo;

    /** Iconos de las mejoras, 16x16 px: un rayo (velocidad), una flecha (salto) y una mira (punteria). */
    public final Texture powerUpVelocidad;
    public final Texture powerUpSalto;
    public final Texture powerUpPunteria;

    public RecursosGraficos() {
        jugador = cargar("sprites/jugador.png");
        espada = cargar("sprites/espada.png");
        brazo = cargar("sprites/brazo.png");
        piso = cargar("sprites/piso.png");
        pistola = cargar("sprites/pistola.png");
        escopeta = cargar("sprites/escopeta.png");
        brillo = cargar("sprites/brillo.png");
        powerUpVelocidad = cargar("sprites/powerup_velocidad.png");
        powerUpSalto = cargar("sprites/powerup_salto.png");
        powerUpPunteria = cargar("sprites/powerup_punteria.png");
        // el halo es un degrade: con Nearest se veria en escalones
        brillo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    /**
     * Carga una textura desde la carpeta {@code assets/} del proyecto.
     *
     * <p>La ruta es relativa a {@code assets/} y siempre usa barras normales, incluso en Windows.
     * {@code Gdx.files.internal} busca primero en el directorio de trabajo y despues en el
     * classpath, por lo que funciona tanto al ejecutar desde el IDE como desde el .jar empaquetado.
     *
     * <p>El filtro {@code Nearest} mantiene los bordes nitidos al escalar. Es lo que se quiere para
     * arte de pixeles: el filtro por defecto ({@code Linear}) los deja borrosos.
     */
    private static Texture cargar(String ruta) {
        Texture textura = new Texture(Gdx.files.internal(ruta));
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return textura;
    }

    @Override
    public void dispose() {
        jugador.dispose();
        espada.dispose();
        brazo.dispose();
        piso.dispose();
        pistola.dispose();
        escopeta.dispose();
        brillo.dispose();
        powerUpVelocidad.dispose();
        powerUpSalto.dispose();
        powerUpPunteria.dispose();
    }
}
