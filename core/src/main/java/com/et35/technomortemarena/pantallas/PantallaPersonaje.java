package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.PerfilJugador;
import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.TechnoMortemArena;

public class PantallaPersonaje extends ScreenAdapter {
    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color COLOR_CAMPO = new Color(0.14f, 0.15f, 0.21f, 1f);
    private static final float ESCALA_VISTA = 2f;
    private static final float Y_PISO_VISTA = 150f;
    private static final float X_VISTA = 640f;
    private static final float LADO_COLOR = 36f;
    private static final float SEPARACION_COLOR = 10f;
    private static final float Y_COLORES = 150f;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final PerfilJugador perfil;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    private final Rectangle campoNombre = new Rectangle(120f, 300f, 360f, 44f);
    private final Rectangle rectAux = new Rectangle();
    private final Rectangle botonVolver = new Rectangle(ANCHO_MUNDO / 2f - 150f, 60f, 300f, 50f);

    private final StringBuilder nombre;
    private final Rectangle[] cuadrosColor = new Rectangle[PerfilJugador.PALETA.length];

    public PantallaPersonaje(RecursosGraficos recursos, RecursosAudio audio) {
        this.recursos = recursos;
        this.audio = audio;
        this.perfil = TechnoMortemArena.getPerfil();
        this.nombre = new StringBuilder(perfil.getNombre());
        for (int i = 0; i < cuadrosColor.length; i++) {
            cuadrosColor[i] = new Rectangle(campoNombre.x + i * (LADO_COLOR + SEPARACION_COLOR), Y_COLORES,
                LADO_COLOR, LADO_COLOR);
        }
        audio.pedirMusica(RecursosAudio.Musica.MENU);
        ui.setAudio(audio);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char caracter) {
                escribir(caracter);
                return true;
            }
        });
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    private void escribir(char caracter) {
        if (caracter == '\b') {
            if (nombre.length() > 0) {
                nombre.setLength(nombre.length() - 1);
            }
        } else if (nombre.length() < PerfilJugador.LARGO_MAXIMO && PerfilJugador.esCaracterValido(caracter)) {
            nombre.append(Character.toUpperCase(caracter));
        } else {
            return;
        }
        perfil.setNombre(nombre.toString());
    }

    @Override
    public void render(float delta) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreVolver = botonVolver.contains(mouse);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || (Gdx.input.justTouched() && sobreVolver)) {
            ui.clic();
            cambiarA(new PantallaMenu(recursos, audio));
            return;
        }
        int cantidad = PerfilJugador.PALETA.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            perfil.setColor((perfil.getColor() + cantidad - 1) % cantidad);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            perfil.setColor((perfil.getColor() + 1) % cantidad);
        } else if (Gdx.input.justTouched()) {
            for (int i = 0; i < cuadrosColor.length; i++) {
                if (cuadrosColor[i].contains(mouse)) {
                    ui.clic();
                    perfil.setColor(i);
                }
            }
        }

        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        String titulo = "PERSONAJE";
        ui.texto(batch, titulo, (ANCHO_MUNDO - ui.anchoTexto(titulo, 2.5f)) / 2f, ALTO_MUNDO - 60f, 2.5f, Color.WHITE);
        ui.texto(batch, "NOMBRE", campoNombre.x, campoNombre.y + 66f, 1.1f, Color.LIGHT_GRAY);
        ui.rectangulo(batch, campoNombre, COLOR_CAMPO);
        String mostrado = nombre + ((int) (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        ui.texto(batch, mostrado, campoNombre.x + 12f, campoNombre.y + 30f, 1.3f, Color.WHITE);
        ui.texto(batch, "HASTA " + PerfilJugador.LARGO_MAXIMO + " LETRAS. SE VE ARRIBA DE TU",
            campoNombre.x, campoNombre.y - 24f, 1f, Color.GRAY);
        ui.texto(batch, "PERSONAJE, EN EL HUD Y AL GANAR.", campoNombre.x, campoNombre.y - 46f, 1f, Color.GRAY);
        dibujarPaleta();
        dibujarVista();
        ui.boton(batch, botonVolver, sobreVolver, "VOLVER", 1.3f);
        batch.end();
    }

    private void dibujarPaleta() {
        int elegido = perfil.getColor();
        ui.texto(batch, "COLOR: " + PerfilJugador.NOMBRES_COLORES[elegido], campoNombre.x, Y_COLORES + LADO_COLOR + 34f,
            1.1f, Color.LIGHT_GRAY);
        for (int i = 0; i < cuadrosColor.length; i++) {
            Rectangle cuadro = cuadrosColor[i];
            if (i == elegido) {
                ui.rectangulo(batch, rectAux.set(cuadro.x - 4f, cuadro.y - 4f, cuadro.width + 8f, cuadro.height + 8f),
                    Color.WHITE);
            }
            ui.rectangulo(batch, cuadro, PerfilJugador.PALETA[i]);
        }
        ui.texto(batch, "CLIC O FLECHAS. SI EL RIVAL TIENE EL MISMO, TE TOCA OTRO.", campoNombre.x, Y_COLORES - 18f,
            0.9f, Color.GRAY);
    }

    private void dibujarVista() {
        float ancho = 48f * ESCALA_VISTA;
        float alto = 96f * ESCALA_VISTA;
        batch.setColor(0.35f, 0.38f, 0.45f, 1f);
        batch.draw(recursos.piso, X_VISTA - 120f, Y_PISO_VISTA - 16f, ancho + 240f, 16f);
        Texture cuerpo = recursos.jugador;
        Color tinte = PerfilJugador.PALETA[perfil.getColor()];
        batch.setColor(tinte);
        batch.draw(cuerpo, X_VISTA, Y_PISO_VISTA, ancho, alto);
        batch.setColor(Color.WHITE);
        String texto = PerfilJugador.mostrar(nombre.toString(), "JUGADOR 1");
        float escala = 1.3f;
        ui.texto(batch, texto, X_VISTA + ancho / 2f - ui.anchoTexto(texto, escala) / 2f, Y_PISO_VISTA + alto + 34f,
            escala, tinte);
    }

    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        ui.dispose();
    }
}
