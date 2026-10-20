package com.et35.technomortemarena.red;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.entrada.EntradaJugador;
import org.junit.jupiter.api.Test;

class EntradaRemotaTest {

    private final EntradaRemota remota = new EntradaRemota();
    private final EntradaJugador salida = new EntradaJugador();

    private void paquete(int secuencia, boolean izq, boolean der, boolean est, int salto, int subir, int bajar) {
        remota.aplicar(secuencia, izq, der, est, (byte) salto, (byte) subir, (byte) bajar);
    }

    @Test
    void losEstadosSeCopianDelUltimoPaquete() {
        paquete(1, true, false, true, 0, 0, 0);
        remota.leer(salida);

        assertTrue(salida.izquierda);
        assertFalse(salida.derecha);
        assertTrue(salida.estocada);
    }

    @Test
    void unaAccionDeUnaSolaVezValeParaUnUnicoFotograma() {
        paquete(1, false, false, false, 1, 0, 0);

        remota.leer(salida);
        assertTrue(salida.saltar);

        remota.leer(salida);
        assertFalse(salida.saltar, "el salto no debe repetirse mientras el contador no cambie");
    }

    @Test
    void unPaqueteDuplicadoNoRepiteLaAccion() {
        paquete(1, false, false, false, 1, 0, 0);
        remota.leer(salida);

        paquete(1, false, false, false, 1, 0, 0);
        remota.leer(salida);

        assertFalse(salida.saltar);
    }

    @Test
    void unPaqueteViejoSeDescarta() {
        paquete(5, true, false, false, 0, 0, 0);

        boolean aceptado = remota.aplicar(4, false, true, false, (byte) 1, (byte) 0, (byte) 0);

        assertFalse(aceptado);
        remota.leer(salida);
        assertTrue(salida.izquierda);
        assertFalse(salida.derecha);
        assertFalse(salida.saltar);
    }

    @Test
    void siSePierdeUnPaqueteElSiguienteTraeLaAccion() {
        paquete(1, false, false, false, 0, 0, 0);
        // el paquete 2, con el salto, se pierde; llega el 3 con el contador ya subido
        paquete(3, false, false, false, 1, 0, 0);

        remota.leer(salida);

        assertTrue(salida.saltar);
    }

    @Test
    void laGuardiaSubeYBajaPorSeparado() {
        paquete(1, false, false, false, 0, 1, 0);
        remota.leer(salida);
        assertTrue(salida.subirEspada);
        assertFalse(salida.bajarEspada);

        paquete(2, false, false, false, 0, 1, 1);
        remota.leer(salida);
        assertFalse(salida.subirEspada);
        assertTrue(salida.bajarEspada);
    }

    @Test
    void elContadorFuncionaCuandoDaLaVuelta() {
        paquete(1, false, false, false, 255, 0, 0);
        remota.leer(salida);

        paquete(2, false, false, false, 0, 0, 0);
        remota.leer(salida);

        assertTrue(salida.saltar, "de 255 a 0 es una accion nueva");
    }

    @Test
    void unContadorQueRetrocedeNoCuentaComoAccion() {
        paquete(1, false, false, false, 10, 0, 0);
        remota.leer(salida);

        paquete(2, false, false, false, 9, 0, 0);
        remota.leer(salida);

        assertFalse(salida.saltar);
    }
}
