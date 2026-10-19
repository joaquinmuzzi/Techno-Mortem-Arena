package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AlturaEspadaTest {

    @Test
    void subirPasaDeCaderaAPechoYDePechoACabeza() {
        assertEquals(AlturaEspada.PECHO, AlturaEspada.CADERA.subir());
        assertEquals(AlturaEspada.CABEZA, AlturaEspada.PECHO.subir());
    }

    @Test
    void bajarPasaDeCabezaAPechoYDePechoACadera() {
        assertEquals(AlturaEspada.PECHO, AlturaEspada.CABEZA.bajar());
        assertEquals(AlturaEspada.CADERA, AlturaEspada.PECHO.bajar());
    }

    @Test
    void enLosExtremosLaGuardiaSeQuedaDondeEsta() {
        assertEquals(AlturaEspada.CABEZA, AlturaEspada.CABEZA.subir());
        assertEquals(AlturaEspada.CADERA, AlturaEspada.CADERA.bajar());
    }

    @Test
    void lasAlturasSeDibujanDeAbajoHaciaArriba() {
        assertTrue(AlturaEspada.CADERA.getDesplazamientoY() < AlturaEspada.PECHO.getDesplazamientoY());
        assertTrue(AlturaEspada.PECHO.getDesplazamientoY() < AlturaEspada.CABEZA.getDesplazamientoY());
    }
}
