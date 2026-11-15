package com.et35.technomortemarena;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import com.et35.technomortemarena.pantallas.PantallaMenu;

public class TechnoMortemArena extends Game {
    private static PerfilJugador perfil;

    private RecursosGraficos recursos;
    private RecursosAudio audio;

    public static PerfilJugador getPerfil() {
        return perfil;
    }

    @Override
    public void create() {
        recursos = new RecursosGraficos();
        audio = new RecursosAudio();
        Preferences preferencias = Gdx.app.getPreferences("techno-mortem-arena");
        perfil = new PerfilJugador(new PerfilJugador.Almacen() {
            @Override
            public String leer(String clave, String porDefecto) {
                return preferencias.getString(clave, porDefecto);
            }

            @Override
            public void guardar(String clave, String valor) {
                preferencias.putString(clave, valor);
                preferencias.flush();
            }
        });
        setScreen(new PantallaMenu(recursos, audio));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            alternarPantallaCompleta();
        }
        super.render();
    }

    private void alternarPantallaCompleta() {
        if (Gdx.graphics.isFullscreen()) {
            Graphics.DisplayMode escritorio = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setWindowedMode(Math.min(1920, escritorio.width), Math.min(1080, escritorio.height));
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        recursos.dispose();
        audio.dispose();
        super.dispose();
    }
}
