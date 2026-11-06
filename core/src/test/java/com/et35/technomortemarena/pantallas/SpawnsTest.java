package com.et35.technomortemarena.pantallas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.et35.technomortemarena.mundo.Arena;
import org.junit.jupiter.api.Test;

class SpawnsTest {
    @Test
    void enElPisoPlanoApoyaSobreElPiso() {
        assertEquals(Arena.FILAS_PISO * Arena.CELDA,
            PantallaArena.alturaDelPiso(Arena.porDefecto(), PantallaArena.DISTANCIA_SPAWN_A_LA_PARED * Arena.CELDA), 0.01f);
    }

    @Test
    void enLaEscaleraApareceSobreElEscalonYNoAdentro() {
        float x = PantallaArena.DISTANCIA_SPAWN_A_LA_PARED * Arena.CELDA;
        assertEquals(6f * Arena.CELDA, PantallaArena.alturaDelPiso(Arena.conPlataformas(), x), 0.01f);
    }

    @Test
    void sinNadaDebajoUsaLaAlturaDelPisoDeSiempre() {
        float x = Arena.ANCHO_MUNDO / 2f - 20f;
        assertEquals(Arena.FILAS_PISO * Arena.CELDA, PantallaArena.alturaDelPiso(Arena.conAgujero(), x), 0.01f);
    }
}
