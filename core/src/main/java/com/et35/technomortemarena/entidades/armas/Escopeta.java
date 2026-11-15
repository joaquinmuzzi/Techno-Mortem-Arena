package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Texture;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.DefinicionProyectil;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entidades.PowerUp;
import com.et35.technomortemarena.entidades.Proyectil;
import java.util.List;
import java.util.Random;

public class Escopeta extends ArmaDeFuego {
    public static final DefinicionProyectil PERDIGON = new DefinicionProyectil(DefinicionProyectil.Aspecto.PERDIGON)
        .velocidad(1300f).gravedad(-250f).lado(8f).danio(1).alcance(0.55f).seRefleja().ignoraInvulnerabilidad();
    public static final int PERDIGONES = 5;
    public static final float ABANICO = 0.3f;
    public static final float DISPERSION = 0.05f;
    private static final float VARIACION_VELOCIDAD = 0.1f;

    public Escopeta() {
        super("Escopeta", 3, 0.6f, PERDIGON, 46f, 12f, 64f);
    }

    @Override
    public void disparar(Jugador quien, float x, float y, float angulo, Random azar, boolean punteria,
                         List<Proyectil> salida) {
        float abanico = punteria ? PowerUp.FACTOR_ABANICO : 1f;
        for (int i = 0; i < PERDIGONES; i++) {
            float desvio = abanico * (ABANICO * ((float) i / (PERDIGONES - 1) - 0.5f)
                + (azar.nextFloat() * 2f - 1f) * DISPERSION);
            float velocidad = PERDIGON.getVelocidad() * (1f + (azar.nextFloat() * 2f - 1f) * VARIACION_VELOCIDAD);
            salida.add(new Proyectil(PERDIGON, quien, x, y, angulo + desvio, velocidad));
        }
    }

    @Override
    public SonidoDisparo getSonidoDisparo() {
        return SonidoDisparo.ESCOPETA;
    }

    @Override
    public Texture getTextura(RecursosGraficos recursos) {
        return recursos.escopeta;
    }
}
