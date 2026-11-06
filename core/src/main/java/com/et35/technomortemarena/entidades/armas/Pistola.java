package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Texture;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.DefinicionProyectil;

public class Pistola extends ArmaDeFuego {
    public static final DefinicionProyectil BALA = new DefinicionProyectil(DefinicionProyectil.Aspecto.BALA)
        .velocidad(1500f).gravedad(-250f).lado(8f).danio(2).seRefleja();

    public Pistola() {
        super("Pistola", 6, 0.22f, BALA, 30f, 12f, 40f);
    }

    @Override
    public SonidoDisparo getSonidoDisparo() {
        return SonidoDisparo.PISTOLA;
    }

    @Override
    public Texture getTextura(RecursosGraficos recursos) {
        return recursos.pistola;
    }
}
