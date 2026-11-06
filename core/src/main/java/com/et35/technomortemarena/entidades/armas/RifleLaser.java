package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Texture;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.DefinicionProyectil;

public class RifleLaser extends ArmaDeFuego {
    public static final DefinicionProyectil RAYO = new DefinicionProyectil(DefinicionProyectil.Aspecto.LASER)
        .velocidad(3000f).gravedad(0f).lado(8f).danio(1).ignoraInvulnerabilidad();

    public RifleLaser() {
        super("Rifle laser", 20, 0.12f, RAYO, 50f, 12f, 66f);
    }

    @Override
    public Texture getTextura(RecursosGraficos recursos) {
        return recursos.rifleLaser;
    }
}
