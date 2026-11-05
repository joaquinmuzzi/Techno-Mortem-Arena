package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.mundo.Arena;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Las armas fuera de las manos durante una ronda: las balas y las armas tiradas que vuelan, y las armas
 * que aparecen en el mapa para que los duelistas las agarren.
 *
 * <p>Cada {@link #INTERVALO_APARICION} segundos, si hay menos de {@link #MAX_EN_EL_SUELO} armas de fuego en
 * el suelo, aparece una pistola o una escopeta (la que haya menos en el suelo) en un lugar al azar sobre el
 * piso o una plataforma, fuera de las zonas prohibidas (los spawns). Quien pasa por encima la agarra y reemplaza lo que tenia en la mano
 * (si tenia la espada, la espada cae a sus pies): la idea es que las armas cambien todo el tiempo. Las
 * armas tiradas o que se caen de la mano quedan en el suelo con las balas que tenian; las espadas solo
 * las agarra quien tiene las manos vacias. Lo simula el anfitrion (o el modo de prueba); el cliente solo dibuja lo que le llega.
 */
public final class ArmasEnJuego {

    /** Segundos entre una aparicion y la siguiente. */
    public static final float INTERVALO_APARICION = 8f;
    public static final int MAX_EN_EL_SUELO = 2;
    private static final float ESCALA = Arena.ESCALA;
    /** Caja de un arma de fuego en el suelo (la de la espada es su largo): con eso se agarra. */
    public static final float ANCHO_EN_EL_SUELO = 56f * ESCALA;
    public static final float ALTO_EN_EL_SUELO = 22f * ESCALA;
    /** Medidas con que se dibujan la pistola y la escopeta, en el suelo y en vuelo. */
    private static final float ANCHO_PISTOLA = 40f * ESCALA;
    private static final float ANCHO_ESCOPETA = 64f * ESCALA;
    private static final float ALTO_ARMA = 18f * ESCALA;
    /** Segundos que un arma recien caida no se puede agarrar: si no, quien la solto la levantaria al instante. */
    static final float ESPERA_PARA_AGARRAR = 0.8f;
    private static final Color COLOR_BALA = new Color(1f, 0.85f, 0.3f, 1f);

    private final List<Proyectil> proyectiles = new ArrayList<>();
    /** Cada una como {@code {codigo de TipoArma, x, y, segundos hasta que se pueda agarrar, balas}}. */
    private final List<float[]> enElSuelo = new ArrayList<>();
    private final List<Proyectil> nuevos = new ArrayList<>();
    private final Random azar;
    private final List<float[]> zonasProhibidas;
    private float tiempoParaAparicion = INTERVALO_APARICION;
    private float tiempoAnimacion;

    /** @param zonasProhibidas zonas {@code {x0, y0, x1, y1}} en celdas donde no aparecen armas */
    public ArmasEnJuego(Random azar, List<float[]> zonasProhibidas) {
        this.azar = azar;
        this.zonasProhibidas = new ArrayList<>(zonasProhibidas);
    }

    /** Saca todo del mapa: se usa al empezar cada ronda. */
    public void limpiar() {
        proyectiles.clear();
        enElSuelo.clear();
        tiempoParaAparicion = INTERVALO_APARICION;
    }

    /**
     * Un fotograma: junta lo que lanzaron los duelistas, mueve los proyectiles, hace aparecer armas y deja
     * que los duelistas las agarren. Se llama despues de actualizar a los dos duelistas.
     */
    public void actualizar(float delta, Arena arena, Jugador uno, Jugador dos) {
        tiempoAnimacion += delta;
        nuevos.clear();
        uno.tomarLanzados(nuevos);
        if (dos != null) {
            dos.tomarLanzados(nuevos);
        }
        proyectiles.addAll(nuevos);
        for (Iterator<Proyectil> it = proyectiles.iterator(); it.hasNext(); ) {
            Proyectil proyectil = it.next();
            proyectil.actualizar(delta, arena, uno, dos);
            if (!proyectil.isActivo()) {
                if (proyectil.dejaArmaEnElSuelo()) {
                    // el arma termino de rebotar: queda apoyada donde freno, con las balas que tenia
                    TipoArma tipo = proyectil.getArma();
                    dejarEnElSuelo(tipo, proyectil.getX() - anchoEnElSuelo(tipo) / 2f,
                        proyectil.getY() - Proyectil.LADO_LANZADA / 2f, 0f, proyectil.getBalas());
                }
                it.remove();
            }
        }
        juntarSoltadas(uno);
        juntarSoltadas(dos);
        for (float[] arma : enElSuelo) {
            arma[3] = Math.max(0f, arma[3] - delta);
        }
        while (proyectiles.size() > EstadoArmas.MAX_PROYECTILES) {
            proyectiles.remove(0);
        }
        tiempoParaAparicion -= delta;
        if (tiempoParaAparicion <= 0f) {
            tiempoParaAparicion = INTERVALO_APARICION;
            if (armasDeFuegoEnElSuelo() < MAX_EN_EL_SUELO) {
                aparecerArma(arena);
            }
        }
        agarrar(uno);
        agarrar(dos);
    }

    private void agarrar(Jugador jugador) {
        if (jugador == null || !jugador.isVivo() || jugador.isDesarmado()) {
            return;
        }
        Rectangle cuerpo = jugador.getCaja();
        for (Iterator<float[]> it = enElSuelo.iterator(); it.hasNext(); ) {
            float[] arma = it.next();
            TipoArma tipo = TipoArma.porCodigo((int) arma[0]);
            if (arma[3] > 0f || (tipo == TipoArma.ESPADA && jugador.getArma() != null)) {
                continue; // recien caida, o una espada para quien ya tiene algo en la mano
            }
            if (cuerpo.overlaps(new Rectangle(arma[1], arma[2], anchoEnElSuelo(tipo), ALTO_EN_EL_SUELO))) {
                it.remove();
                if (jugador.getArma() == TipoArma.ESPADA) {
                    // cambia la espada por el arma de fuego: la espada queda en el piso
                    dejarEnElSuelo(TipoArma.ESPADA, jugador.getPosicion().x, jugador.getPosicion().y, ESPERA_PARA_AGARRAR);
                }
                jugador.equipar(tipo, (int) arma[4]);
                return;
            }
        }
    }

    /** Las armas que se le cayeron a un duelista por un punetazo quedan a sus pies. */
    private void juntarSoltadas(Jugador jugador) {
        if (jugador == null) {
            return;
        }
        TipoArma soltada = jugador.tomarArmaSoltada();
        if (soltada != null) {
            dejarEnElSuelo(soltada, jugador.getPosicion().x, jugador.getPosicion().y, ESPERA_PARA_AGARRAR,
                jugador.getBalasSoltadas());
        }
    }

    /** Deja un arma recien aparecida (cargada) en el suelo. */
    void dejarEnElSuelo(TipoArma tipo, float x, float y, float espera) {
        dejarEnElSuelo(tipo, x, y, espera, tipo.getBalasIniciales());
    }

    void dejarEnElSuelo(TipoArma tipo, float x, float y, float espera, int balas) {
        enElSuelo.add(new float[] {TipoArma.codigoDe(tipo), x, y, espera, balas});
        while (enElSuelo.size() > EstadoArmas.MAX_EN_EL_SUELO) {
            enElSuelo.remove(0);
        }
    }

    private int armasDeFuegoEnElSuelo() {
        return cuantasHay(TipoArma.PISTOLA) + cuantasHay(TipoArma.ESCOPETA);
    }

    private int cuantasHay(TipoArma tipo) {
        int cantidad = 0;
        for (float[] arma : enElSuelo) {
            if (TipoArma.porCodigo((int) arma[0]) == tipo) {
                cantidad++;
            }
        }
        return cantidad;
    }

    /** Ancho de la caja de un arma en el suelo: la espada es mas larga. */
    static float anchoEnElSuelo(TipoArma tipo) {
        return tipo == TipoArma.ESPADA ? Jugador.ANCHO_ESPADA : ANCHO_EN_EL_SUELO;
    }

    private static float anchoDibujado(TipoArma tipo) {
        return tipo == TipoArma.ESCOPETA ? ANCHO_ESCOPETA : ANCHO_PISTOLA;
    }

    /** Pone una pistola o una escopeta sobre una superficie al azar. Sin lugar (un mapa sin piso), no pone nada. */
    void aparecerArma(Arena arena) {
        List<float[]> lugares = superficies(arena);
        if (lugares.isEmpty()) {
            return;
        }
        float[] lugar = lugares.get(azar.nextInt(lugares.size()));
        // la que haya menos en el suelo: asi con dos pistolas tiradas igual aparece una escopeta
        int pistolas = cuantasHay(TipoArma.PISTOLA);
        int escopetas = cuantasHay(TipoArma.ESCOPETA);
        TipoArma tipo;
        if (pistolas != escopetas) {
            tipo = pistolas < escopetas ? TipoArma.PISTOLA : TipoArma.ESCOPETA;
        } else {
            tipo = azar.nextBoolean() ? TipoArma.PISTOLA : TipoArma.ESCOPETA;
        }
        dejarEnElSuelo(tipo, lugar[0], lugar[1], 0f);
    }

    /**
     * Las celdas donde puede aparecer un arma: arriba de un bloque solido (no un pincho, trampolin ni
     * cinta), con aire encima y fuera de las zonas prohibidas. Devuelve la esquina inferior izquierda.
     */
    List<float[]> superficies(Arena arena) {
        String[] filas = arena.getFilas();
        List<float[]> lugares = new ArrayList<>();
        int alto = filas.length;
        for (int fila = 0; fila < alto; fila++) {
            String texto = filas[alto - 1 - fila];
            // sin las columnas de los bordes: pegada a la pared casi no se ve
            for (int columna = 2; columna + 3 < Arena.COLUMNAS; columna++) {
                if (texto.charAt(columna) != '#' || texto.charAt(columna + 1) != '#') {
                    continue;
                }
                if (!esAire(filas, columna, fila + 1) || !esAire(filas, columna + 1, fila + 1)
                    || !esAire(filas, columna, fila + 2) || !esAire(filas, columna + 1, fila + 2)) {
                    continue;
                }
                if (enZonaProhibida(columna, fila + 1)) {
                    continue;
                }
                lugares.add(new float[] {columna * Arena.CELDA, (fila + 1) * Arena.CELDA});
            }
        }
        return lugares;
    }

    private static boolean esAire(String[] filas, int columna, int fila) {
        int indice = filas.length - 1 - fila;
        return indice < 0 || filas[indice].charAt(columna) == '.';
    }

    private boolean enZonaProhibida(int columna, int fila) {
        for (float[] zona : zonasProhibidas) {
            if (columna + 2 > zona[0] && columna < zona[2] && fila + 1 > zona[1] && fila < zona[3]) {
                return true;
            }
        }
        return false;
    }

    public List<Proyectil> getProyectiles() {
        return proyectiles;
    }

    /** Las armas en el suelo, como {@code {codigo de TipoArma, x, y}}. */
    public List<float[]> getEnElSuelo() {
        return enElSuelo;
    }

    /** Copia en {@code destino} lo que hay que dibujar en el cliente. */
    public void exportar(EstadoArmas destino) {
        destino.proyectiles.clear();
        for (Proyectil p : proyectiles) {
            destino.proyectiles.add(new float[] {p.getTipo().ordinal(), p.getX(), p.getY(), p.getAngulo()});
        }
        destino.enElSuelo.clear();
        for (float[] arma : enElSuelo) {
            destino.enElSuelo.add(arma.clone());
        }
    }

    /** Pisa lo que hay con lo que mando el anfitrion. Lo usa el cliente, que no simula las armas. */
    public void importar(EstadoArmas origen, float delta) {
        tiempoAnimacion += delta;
        proyectiles.clear();
        for (float[] p : origen.proyectiles) {
            proyectiles.add(Proyectil.paraDibujar(Proyectil.Tipo.values()[(int) p[0]], p[1], p[2], p[3]));
        }
        enElSuelo.clear();
        for (float[] arma : origen.enElSuelo) {
            enElSuelo.add(arma.clone());
        }
    }

    /** Dibuja las armas en el suelo y los proyectiles. Entre {@code batch.begin()} y {@code end()}. */
    public void dibujar(SpriteBatch batch, RecursosGraficos recursos) {
        float flote = (float) Math.sin(tiempoAnimacion * 4f) * 4f * ESCALA;
        for (float[] arma : enElSuelo) {
            TipoArma tipo = TipoArma.porCodigo((int) arma[0]);
            float ancho = tipo == TipoArma.ESPADA ? Jugador.ANCHO_ESPADA : anchoDibujado(tipo);
            float x = arma[1] + (anchoEnElSuelo(tipo) - ancho) / 2f;
            float y = arma[2] + 6f * ESCALA + flote;
            // un halo propio que late detras, para que se vea de lejos que hay algo para agarrar
            float brillo = 0.6f + 0.3f * (float) Math.sin(tiempoAnimacion * 6f);
            batch.setColor(1f, 1f, 1f, brillo);
            float margenX = 18f * ESCALA;
            float margenY = 14f * ESCALA;
            batch.draw(recursos.brillo, x - margenX, y - margenY, ancho + 2f * margenX, ALTO_ARMA + 2f * margenY);
            batch.setColor(Color.WHITE);
            if (tipo == TipoArma.ESPADA) {
                batch.draw(recursos.espada, x, y, ancho, Jugador.ALTO_ESPADA);
            } else {
                batch.draw(texturaDe(recursos, tipo), x, y, ancho, ALTO_ARMA);
            }
        }
        for (Proyectil p : proyectiles) {
            switch (p.getTipo()) {
                case BALA:
                    batch.setColor(COLOR_BALA);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 14f * ESCALA, 4f * ESCALA, p.getAngulo());
                    break;
                case PERDIGON:
                    batch.setColor(COLOR_BALA);
                    dibujarCentrado(batch, recursos.brazo, p.getX(), p.getY(), 7f * ESCALA, 4f * ESCALA, p.getAngulo());
                    break;
                case ESPADA_LANZADA:
                    batch.setColor(Color.WHITE);
                    dibujarCentrado(batch, recursos.espada, p.getX(), p.getY(), Jugador.ANCHO_ESPADA, Jugador.ALTO_ESPADA, p.getAngulo());
                    break;
                default:
                    batch.setColor(Color.WHITE);
                    TipoArma tipo = p.getArma();
                    dibujarCentrado(batch, texturaDe(recursos, tipo), p.getX(), p.getY(), anchoDibujado(tipo), ALTO_ARMA, p.getAngulo());
                    break;
            }
        }
        batch.setColor(Color.WHITE);
    }

    private static Texture texturaDe(RecursosGraficos recursos, TipoArma tipo) {
        return tipo == TipoArma.ESCOPETA ? recursos.escopeta : recursos.pistola;
    }

    private static void dibujarCentrado(SpriteBatch batch, Texture textura, float x, float y, float ancho, float alto, float angulo) {
        batch.draw(textura, x - ancho / 2f, y - alto / 2f, ancho / 2f, alto / 2f, ancho, alto, 1f, 1f,
            angulo * MathUtils.radiansToDegrees, 0, 0, textura.getWidth(), textura.getHeight(), false, false);
    }
}
