package com.et35.technomortemarena;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PerfilJugadorTest {
    private static final class EnMemoria implements PerfilJugador.Almacen {
        final Map<String, String> valores = new HashMap<>();

        @Override
        public String leer(String clave, String porDefecto) {
            return valores.getOrDefault(clave, porDefecto);
        }

        @Override
        public void guardar(String clave, String valor) {
            valores.put(clave, valor);
        }
    }

    @Test
    void porDefectoNoHayNombreYSeMuestraElDelLugar() {
        PerfilJugador perfil = new PerfilJugador(new EnMemoria());
        assertEquals("", perfil.getNombre());
        assertEquals("JUGADOR 1", PerfilJugador.mostrar(perfil.getNombre(), "JUGADOR 1"));
    }

    @Test
    void elNombreQuedaGuardadoParaLaProximaVez() {
        EnMemoria almacen = new EnMemoria();
        new PerfilJugador(almacen).setNombre("Joaco");
        assertEquals("JOACO", new PerfilJugador(almacen).getNombre());
    }

    @Test
    void limpiarPasaAMayusculasYSacaLoQueNoVa() {
        assertEquals("NIÑO-2", PerfilJugador.limpiar("niño-2"));
        assertEquals("AB", PerfilJugador.limpiar("a!@#b\n"));
        assertEquals("LA MUERTE", PerfilJugador.limpiar("  la   muerte  "));
        assertEquals("", PerfilJugador.limpiar(null));
        assertEquals("", PerfilJugador.limpiar("   "));
    }

    @Test
    void elNombreNoPasaDelLargoMaximo() {
        String largo = PerfilJugador.limpiar("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        assertEquals(PerfilJugador.LARGO_MAXIMO, largo.length());
        assertEquals("ABCDEFGHIJKL", largo);
    }

    @Test
    void unNombreGuardadoAManoSeLimpiaAlLeerlo() {
        EnMemoria almacen = new EnMemoria();
        almacen.valores.put(PerfilJugador.CLAVE_NOMBRE, "hola<script>");
        assertEquals("HOLASCRIPT", new PerfilJugador(almacen).getNombre());
    }
}
