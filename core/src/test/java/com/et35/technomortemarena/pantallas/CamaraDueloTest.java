package com.et35.technomortemarena.pantallas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;
import org.junit.jupiter.api.Test;

class CamaraDueloTest {
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;

    private static Rectangle cuerpoEn(float x) {
        return new Rectangle(x, PISO, 96f, 192f);
    }

    private static float[] vista(CamaraDuelo camara) {
        float medioAncho = Arena.ANCHO_VISTA * camara.getZoom() / 2f;
        float medioAlto = Arena.ALTO_VISTA * camara.getZoom() / 2f;
        return new float[] {camara.getX() - medioAncho, camara.getY() - medioAlto,
            camara.getX() + medioAncho, camara.getY() + medioAlto};
    }

    @Test
    void conLosDuelistasCercaMuestraLaVistaNormal() {
        CamaraDuelo camara = new CamaraDuelo();
        camara.seguir(1f / 60f, cuerpoEn(3500f), cuerpoEn(4000f), false);
        assertEquals(1f, camara.getZoom(), 0.0001f);
        assertEquals((3500f + 4096f) / 2f, camara.getX(), 1f, "centrada entre los dos");
    }

    @Test
    void cuandoSeSeparanSeAlejaYLosDosEntran() {
        CamaraDuelo camara = new CamaraDuelo();
        Rectangle uno = cuerpoEn(500f);
        Rectangle dos = cuerpoEn(6500f);
        camara.seguir(1f / 60f, uno, dos, false);
        assertTrue(camara.getZoom() > 1f);
        float[] v = vista(camara);
        assertTrue(v[0] <= uno.x && v[2] >= dos.x + dos.width, "los dos en pantalla");
    }

    @Test
    void nuncaSeAlejaMasQueElMapaEnteroNiMuestraAfuera() {
        CamaraDuelo camara = new CamaraDuelo();
        camara.seguir(1f / 60f, cuerpoEn(0f), cuerpoEn(Arena.ANCHO_MUNDO - 96f), false);
        assertEquals(CamaraDuelo.ZOOM_MAXIMO, camara.getZoom(), 0.0001f);
        float[] v = vista(camara);
        assertEquals(0f, v[0], 0.5f);
        assertEquals(Arena.ANCHO_MUNDO, v[2], 0.5f);
        assertEquals(0f, v[1], 0.5f);
    }

    @Test
    void cercaDeUnBordeLaCamaraSeQuedaPegadaAlBorde() {
        CamaraDuelo camara = new CamaraDuelo();
        camara.seguir(1f / 60f, cuerpoEn(10f), cuerpoEn(200f), false);
        float[] v = vista(camara);
        assertEquals(0f, v[0], 0.5f, "no muestra lo que hay a la izquierda del mapa");
        assertEquals(0f, v[1], 0.5f, "ni debajo del piso");
    }

    @Test
    void enLaEdicionMuestraElMapaEntero() {
        CamaraDuelo camara = new CamaraDuelo();
        camara.seguir(1f / 60f, cuerpoEn(3500f), cuerpoEn(4000f), true);
        assertEquals(CamaraDuelo.ZOOM_MAXIMO, camara.getZoom(), 0.0001f);
        assertEquals(Arena.ANCHO_MUNDO / 2f, camara.getX(), 0.5f);
        assertEquals(Arena.ALTO_MUNDO / 2f, camara.getY(), 0.5f);
    }

    @Test
    void despuesDelPrimerFotogramaSeMueveConSuavidad() {
        CamaraDuelo camara = new CamaraDuelo();
        camara.seguir(1f / 60f, cuerpoEn(3500f), cuerpoEn(4000f), false);
        float antes = camara.getX();
        camara.seguir(1f / 60f, cuerpoEn(5000f), cuerpoEn(5500f), false);
        assertTrue(camara.getX() > antes && camara.getX() < (5000f + 5596f) / 2f, "va hacia ahi, sin saltar");
    }

    @Test
    void sinSegundoDuelistaSigueAlPrimero() {
        CamaraDuelo camara = new CamaraDuelo();
        camara.seguir(1f / 60f, cuerpoEn(3000f), null, false);
        assertEquals(1f, camara.getZoom(), 0.0001f);
        assertEquals(3048f, camara.getX(), 1f);
    }
}
