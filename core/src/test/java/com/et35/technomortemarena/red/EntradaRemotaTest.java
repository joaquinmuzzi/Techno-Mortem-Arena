package com.et35.technomortemarena.red;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.entrada.EntradaJugador;
import org.junit.jupiter.api.Test;

class EntradaRemotaTest {

    private final EntradaRemota remota = new EntradaRemota();
    private final EntradaJugador salida = new EntradaJugador();

    private void paquete(int secuencia, boolean izq, boolean der, int salto, int estocada) {
        remota.aplicar(secuencia, izq, der, true, (byte) salto, (byte) estocada, 0f, 0f);
    }

    @Test
    void losEstadosSeCopianDelUltimoPaquete() {
        paquete(1, true, false, 0, 0);
        remota.leer(salida);

        assertTrue(salida.izquierda);
        assertFalse(salida.derecha);
    }

    @Test
    void elPuntoApuntadoSeCopiaYSeMantiene() {
        remota.aplicar(1, false, false, true, (byte) 0, (byte) 0, 300f, 200f);

        remota.leer(salida);
        assertTrue(salida.hayPuntero);
        assertEquals(300f, salida.apuntaX);
        assertEquals(200f, salida.apuntaY);

        remota.leer(salida);
        assertEquals(300f, salida.apuntaX, "sin paquetes nuevos sigue apuntando al mismo lado");
    }

    @Test
    void sinPunteroNoSeApunta() {
        remota.aplicar(1, false, false, false, (byte) 0, (byte) 0, 300f, 200f);

        remota.leer(salida);

        assertFalse(salida.hayPuntero);
    }

    @Test
    void unaAccionDeUnaSolaVezValeParaUnUnicoFotograma() {
        paquete(1, false, false, 1, 0);

        remota.leer(salida);
        assertTrue(salida.saltar);

        remota.leer(salida);
        assertFalse(salida.saltar, "el salto no debe repetirse mientras el contador no cambie");
    }

    @Test
    void unPaqueteDuplicadoNoRepiteLaAccion() {
        paquete(1, false, false, 0, 1);
        remota.leer(salida);
        assertTrue(salida.estocada);

        paquete(1, false, false, 0, 1);
        remota.leer(salida);

        assertFalse(salida.estocada);
    }

    @Test
    void unPaqueteViejoSeDescarta() {
        paquete(5, true, false, 0, 0);

        boolean aceptado = remota.aplicar(4, false, true, true, (byte) 1, (byte) 0, 0f, 0f);

        assertFalse(aceptado);
        remota.leer(salida);
        assertTrue(salida.izquierda);
        assertFalse(salida.derecha);
        assertFalse(salida.saltar);
    }

    @Test
    void siSePierdeUnPaqueteElSiguienteTraeLaAccion() {
        paquete(1, false, false, 0, 0);
        // el paquete 2, con la estocada, se pierde; llega el 3 con el contador ya subido
        paquete(3, false, false, 0, 1);

        remota.leer(salida);

        assertTrue(salida.estocada);
    }

    @Test
    void elSaltoYLaEstocadaSeContanPorSeparado() {
        paquete(1, false, false, 1, 0);
        remota.leer(salida);
        assertTrue(salida.saltar);
        assertFalse(salida.estocada);

        paquete(2, false, false, 1, 1);
        remota.leer(salida);
        assertFalse(salida.saltar);
        assertTrue(salida.estocada);
    }

    @Test
    void elContadorFuncionaCuandoDaLaVuelta() {
        paquete(1, false, false, 255, 0);
        remota.leer(salida);

        paquete(2, false, false, 0, 0);
        remota.leer(salida);

        assertTrue(salida.saltar, "de 255 a 0 es una accion nueva");
    }

    @Test
    void unContadorQueRetrocedeNoCuentaComoAccion() {
        paquete(1, false, false, 10, 0);
        remota.leer(salida);

        paquete(2, false, false, 9, 0);
        remota.leer(salida);

        assertFalse(salida.saltar);
    }
}
