package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Texture;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.DefinicionProyectil;

public class Lanzacohetes extends ArmaDeFuego {
    public static final DefinicionProyectil COHETE = new DefinicionProyectil(DefinicionProyectil.Aspecto.COHETE)
        .velocidad(700f).gravedad(-150f).lado(14f).seRefleja().explota();
    public static final DefinicionProyectil EXPLOSION = new DefinicionProyectil(DefinicionProyectil.Aspecto.EXPLOSION);
    public static final int DANIO_EXPLOSION = 6;

    public Lanzacohetes() {
        super("Lanzacohetes", 1, 1f, COHETE, 52f, 16f, 66f);
    }

    @Override
    public Texture getTextura(RecursosGraficos recursos) {
        return recursos.lanzacohetes;
    }
}
