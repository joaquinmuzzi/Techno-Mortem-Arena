package com.et35.technomortemarena.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ArenaTest {

    private static String fila(char caracter, int veces) {
        char[] celdas = new char[veces];
        Arrays.fill(celdas, caracter);
        return new String(celdas);
    }

    private static String vacia() {
        return fila('.', Arena.COLUMNAS);
    }

    @Test
    void laArenaPorDefectoSoloTieneElPiso() {
        Arena arena = Arena.porDefecto();

        assertEquals(Arena.COLUMNAS * Arena.FILAS_PISO, arena.getBloques().size());
        assertEquals(Arena.ANCHO_MUNDO, arena.getAncho());
        assertEquals(Arena.ALTO_MUNDO, arena.getAlto());
    }

    @Test
    void elAgujeroQuitaCeldasDelPiso() {
        int celdasDelAgujero = 18 * Arena.FILAS_PISO;

        Arena arena = Arena.conAgujero();

        assertEquals(Arena.COLUMNAS * Arena.FILAS_PISO - celdasDelAgujero, arena.getBloques().size());
    }

    @Test
    void todosLosMapasPredefinidosTienenBloques() {
        Arena[] mapas = {
            Arena.porDefecto(), Arena.conAgujero(), Arena.conPlataformas(),
            Arena.conTrampolin(), Arena.conCintasYPinchos()
        };

        for (Arena mapa : mapas) {
            assertTrue(mapa.getBloques().size() > 0);
        }
    }

    @Test
    void unaFilaConLargoIncorrectoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> Arena.desdeGrilla(fila('#', Arena.COLUMNAS - 1)));
        assertThrows(IllegalArgumentException.class, () -> Arena.desdeGrilla(fila('#', Arena.COLUMNAS + 1)));
    }

    @Test
    void laPrimeraFilaEsLaDeArribaYLaUltimaLaDelPiso() {
        Arena arena = Arena.desdeGrilla(fila('#', Arena.COLUMNAS), vacia());

        assertEquals(Arena.COLUMNAS, arena.getBloques().size());
        for (Bloque bloque : arena.getBloques()) {
            assertEquals(Arena.CELDA, bloque.getArea().y, 0.001f);
        }
    }

    @Test
    void losSpawnsSalenDeLosMarcadores() {
        String piso = fila('#', Arena.COLUMNAS);
        String conSpawns = "1" + fila('.', Arena.COLUMNAS - 2) + "2";

        Arena arena = Arena.desdeGrilla(conSpawns, piso);

        Vector2 uno = arena.getSpawnUno();
        Vector2 dos = arena.getSpawnDos();
        assertNotNull(uno);
        assertNotNull(dos);
        assertEquals(0f, uno.x, 0.001f);
        assertEquals((Arena.COLUMNAS - 1) * Arena.CELDA, dos.x, 0.001f);
        assertEquals(Arena.CELDA, uno.y, 0.001f);
    }

    @Test
    void sinMarcadoresNoHaySpawns() {
        Arena arena = Arena.porDefecto();

        assertNull(arena.getSpawnUno());
        assertNull(arena.getSpawnDos());
    }

    @Test
    void elSpawnDevueltoEsUnaCopia() {
        Arena arena = Arena.desdeGrilla("1" + fila('.', Arena.COLUMNAS - 1));

        arena.getSpawnUno().x = 999f;

        assertEquals(0f, arena.getSpawnUno().x, 0.001f);
    }

    @Test
    void cadaCaracterCreaSuTipoDeBloque() {
        Rectangle area = new Rectangle(0f, 0f, Arena.CELDA, Arena.CELDA);

        assertEquals(Bloque.class, Arena.bloquePara('#', area).getClass());
        assertEquals(Trampolin.class, Arena.bloquePara('T', area).getClass());
        assertEquals(Pincho.class, Arena.bloquePara('X', area).getClass());
        assertNull(Arena.bloquePara('.', area));
        assertNull(Arena.bloquePara('?', area));
    }

    @Test
    void lasCintasEmpujanHaciaLaIzquierdaYLaDerecha() {
        Rectangle area = new Rectangle(0f, 0f, Arena.CELDA, Arena.CELDA);

        assertTrue(Arena.bloquePara('L', area).getVelocidadHorizontal() < 0f);
        assertTrue(Arena.bloquePara('R', area).getVelocidadHorizontal() > 0f);
        assertEquals(0f, Arena.bloquePara('#', area).getVelocidadHorizontal());
    }

    @Test
    void laListaDeBloquesEsDeSoloLectura() {
        Arena arena = Arena.porDefecto();

        assertThrows(UnsupportedOperationException.class, () -> arena.getBloques().clear());
    }

    @Test
    void limitarXMantieneAlaEntidadDentroDeLasParedes() {
        Arena arena = Arena.porDefecto();
        float ancho = 50f;

        assertEquals(0f, arena.limitarX(-10f, ancho));
        assertEquals(arena.getAncho() - ancho, arena.limitarX(arena.getAncho(), ancho));
        assertEquals(100f, arena.limitarX(100f, ancho));
    }
}
