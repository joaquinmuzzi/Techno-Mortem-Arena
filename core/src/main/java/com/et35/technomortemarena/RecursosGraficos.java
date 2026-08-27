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

    /** Baldosa del piso, 64x64 px, disenada para repetirse en horizontal. */
    public final Texture piso;

    public RecursosGraficos() {
        jugador = cargar("sprites/jugador.png");
        espada = cargar("sprites/espada.png");
        piso = cargar("sprites/piso.png");
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
        piso.dispose();
    }
}
