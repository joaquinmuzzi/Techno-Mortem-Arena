package com.et35.technomortemarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class RecursosGraficos implements Disposable {
    public final Texture jugador;

    public final Texture espada;

    public final Texture brazo;

    public final Texture piso;

    public final Texture pistola;

    public final Texture escopeta;
    public final Texture rifleLaser;
    public final Texture lanzacohetes;
    public final Texture cohete;

    public final Texture brillo;

    public final Texture powerUpVelocidad;
    public final Texture powerUpSalto;
    public final Texture powerUpPunteria;

    public RecursosGraficos() {
        jugador = cargar("sprites/jugador.png");
        espada = cargar("sprites/espada.png");
        brazo = cargar("sprites/brazo.png");
        piso = cargar("sprites/piso.png");
        pistola = cargar("sprites/pistola.png");
        escopeta = cargar("sprites/escopeta.png");
        rifleLaser = cargar("sprites/rifle_laser.png");
        lanzacohetes = cargar("sprites/lanzacohetes.png");
        cohete = cargar("sprites/cohete.png");
        brillo = cargar("sprites/brillo.png");
        powerUpVelocidad = cargar("sprites/powerup_velocidad.png");
        powerUpSalto = cargar("sprites/powerup_salto.png");
        powerUpPunteria = cargar("sprites/powerup_punteria.png");
        brillo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private static Texture cargar(String ruta) {
        Texture textura = new Texture(Gdx.files.internal(ruta));
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return textura;
    }

    @Override
    public void dispose() {
        jugador.dispose();
        espada.dispose();
        brazo.dispose();
        piso.dispose();
        pistola.dispose();
        escopeta.dispose();
        rifleLaser.dispose();
        lanzacohetes.dispose();
        cohete.dispose();
        brillo.dispose();
        powerUpVelocidad.dispose();
        powerUpSalto.dispose();
        powerUpPunteria.dispose();
    }
}
