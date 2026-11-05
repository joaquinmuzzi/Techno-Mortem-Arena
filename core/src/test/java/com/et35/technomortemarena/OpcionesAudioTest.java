package com.et35.technomortemarena;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Prueba las opciones de sonido sin abrir el juego, con un almacen en memoria. */
class OpcionesAudioTest {

    private static final class EnMemoria implements OpcionesAudio.Almacen {
        final Map<String, Boolean> valores = new HashMap<>();

        @Override
        public boolean leer(String clave, boolean porDefecto) {
            return valores.getOrDefault(clave, porDefecto);
        }

        @Override
        public void guardar(String clave, boolean valor) {
            valores.put(clave, valor);
        }
    }

    @Test
    void porDefectoSuenaTodo() {
        OpcionesAudio opciones = new OpcionesAudio(new EnMemoria());
        assertTrue(opciones.isMusica());
        assertTrue(opciones.isEfectos());
    }

    @Test
    void loQueSeApagaQuedaGuardadoParaLaProximaVez() {
        EnMemoria almacen = new EnMemoria();
        OpcionesAudio opciones = new OpcionesAudio(almacen);
        opciones.setMusica(false);
        opciones.setEfectos(false);

        OpcionesAudio despues = new OpcionesAudio(almacen);
        assertFalse(despues.isMusica());
        assertFalse(despues.isEfectos());

        despues.setMusica(true);
        assertTrue(new OpcionesAudio(almacen).isMusica());
        assertFalse(new OpcionesAudio(almacen).isEfectos());
    }
}
