package com.et35.technomortemarena.entidades.armas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.entidades.Proyectil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ArmasTest {
    private static List<Proyectil> disparo(Arma arma, boolean punteria) {
        List<Proyectil> salida = new ArrayList<>();
        arma.disparar(null, 100f, 200f, 0f, new Random(1), punteria, salida);
        return salida;
    }

    @Test
    void cadaArmaDeFuegoDisparaSuPropioProyectil() {
        assertSame(Pistola.BALA, disparo(Armas.PISTOLA, false).get(0).getDefinicion());
        assertSame(RifleLaser.RAYO, disparo(Armas.RIFLE_LASER, false).get(0).getDefinicion());
        assertSame(Lanzacohetes.COHETE, disparo(Armas.LANZACOHETES, false).get(0).getDefinicion());
        assertEquals(1, disparo(Armas.PISTOLA, false).size());
    }

    @Test
    void laEscopetaSobrescribeElDisparoConUnAbanico() {
        List<Proyectil> perdigones = disparo(Armas.ESCOPETA, false);
        assertEquals(Escopeta.PERDIGONES, perdigones.size());
        float minimo = Float.MAX_VALUE;
        float maximo = -Float.MAX_VALUE;
        for (Proyectil p : perdigones) {
            assertSame(Escopeta.PERDIGON, p.getDefinicion());
            minimo = Math.min(minimo, p.getAngulo());
            maximo = Math.max(maximo, p.getAngulo());
        }
        assertTrue(maximo - minimo > Escopeta.ABANICO * 0.5f, "abiertos en abanico");
    }

    @Test
    void lasEspadasNoDisparan() {
        assertTrue(disparo(Armas.ESPADA, false).isEmpty());
        assertTrue(disparo(Armas.ESPADA_LASER, false).isEmpty());
        assertFalse(Armas.ESPADA.esDeFuego());
        assertTrue(Armas.ESPADA.esEspada());
    }

    @Test
    void cadaArmaDefineComoSonSusProyectiles() {
        assertEquals(0f, RifleLaser.RAYO.getGravedad(), 0f, "el rayo no cae");
        assertTrue(RifleLaser.RAYO.getVelocidad() > Pistola.BALA.getVelocidad(), "el rayo es mas rapido");
        assertFalse(RifleLaser.RAYO.isSeRefleja(), "el rayo no se refleja");
        assertTrue(Escopeta.PERDIGON.getAlcance() > 0f, "el perdigon se pierde");
        assertTrue(Lanzacohetes.COHETE.isExplota());
        assertEquals(2, Pistola.BALA.getDanio());
    }

    @Test
    void laEspadaLaserCambiaSoloLoQueLaHaceDistinta() {
        assertEquals(6, Armas.ESPADA.getDanioHoja());
        assertEquals(8, Armas.ESPADA_LASER.getDanioHoja());
        assertTrue(Armas.ESPADA.sePuedeSoltar());
        assertFalse(Armas.ESPADA_LASER.sePuedeSoltar());
        assertEquals(1f, Armas.ESPADA_LASER.getVelocidadReflejo(), 0f, "refleja perfecto");
        assertFalse(Armas.ESPADA_LASER.chocaCon(Armas.ESPADA), "atraviesa a la comun");
        assertTrue(Armas.ESPADA_LASER.chocaCon(Armas.ESPADA_LASER));
        assertTrue(Armas.ESPADA.chocaCon(Armas.ESPADA));
        assertTrue(Armas.ESPADA_LASER.getDefinicionLanzada().getGravedad() > Armas.ESPADA.getDefinicionLanzada().getGravedad(),
            "tirada cae menos");
    }

    @Test
    void lasEspadasSiempreQuedanEnElMapaYLasDeFuegoSoloConBalas() {
        assertTrue(Armas.ESPADA.quedaEnElMapa(0));
        assertFalse(Armas.PISTOLA.quedaEnElMapa(0));
        assertTrue(Armas.PISTOLA.quedaEnElMapa(3));
    }

    @Test
    void losCodigosVanYVuelven() {
        for (int codigo = 1; codigo <= 6; codigo++) {
            assertEquals(codigo, Armas.codigoDe(Armas.porCodigo(codigo)));
        }
        assertNull(Armas.porCodigo(0));
        assertEquals(0, Armas.codigoDe(null));
        assertEquals(4, Armas.queAparecen().length);
    }
}
