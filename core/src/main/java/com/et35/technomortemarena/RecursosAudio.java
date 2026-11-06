package com.et35.technomortemarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public class RecursosAudio implements Disposable {
    public final Sound correr;

    public final Sound golpe;

    public final Sound aterrizar;

    public final Sound pistolazo;

    public final Sound escopetazo;

    public final Sound espadaEquipada;

    public final Sound espadaCae;

    public final Sound armaEquipada;

    public final Sound boton;

    public final Sound botonSeleccionado;

    public enum Musica { NINGUNA, MENU, COMBATE }

    private static final float VOLUMEN_MUSICA = 0.5f;

    private final Music musicaMenu;
    private final Music musicaCombate;
    private final OpcionesAudio opciones;
    private Musica pedida = Musica.NINGUNA;

    public RecursosAudio() {
        correr = cargar("sounds/Running.mp3");
        golpe = cargar("sounds/Bloody Sword Hit Sound Effect.mp3");
        aterrizar = cargar("sounds/Landing.wav");
        pistolazo = cargar("sounds/pistol-shot.mp3");
        escopetazo = cargar("sounds/shotgun-blast.mp3");
        espadaEquipada = cargar("sounds/sword-equipped.mp3");
        espadaCae = cargar("sounds/sword-falling.mp3");
        armaEquipada = cargar("sounds/Weapon-Equip.mp3");
        boton = cargar("sounds/Boton.wav");
        botonSeleccionado = cargar("sounds/Boton Seleccionado.mp3");
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("music/menu.mp3"));
        musicaMenu.setLooping(true);
        musicaMenu.setVolume(VOLUMEN_MUSICA);
        musicaCombate = Gdx.audio.newMusic(Gdx.files.internal("music/combate.mp3"));
        musicaCombate.setLooping(true);
        musicaCombate.setVolume(VOLUMEN_MUSICA);
        Preferences preferencias = Gdx.app.getPreferences("techno-mortem-arena");
        opciones = new OpcionesAudio(new OpcionesAudio.Almacen() {
            @Override
            public boolean leer(String clave, boolean porDefecto) {
                return preferencias.getBoolean(clave, porDefecto);
            }

            @Override
            public void guardar(String clave, boolean valor) {
                preferencias.putBoolean(clave, valor);
                preferencias.flush();
            }
        });
    }

    public OpcionesAudio getOpciones() {
        return opciones;
    }

    public void reproducir(Sound sonido, float volumen) {
        if (opciones.isEfectos()) {
            sonido.play(volumen);
        }
    }

    public void repetir(Sound sonido, float volumen) {
        if (opciones.isEfectos()) {
            sonido.loop(volumen);
        }
    }

    public void pedirMusica(Musica musica) {
        pedida = musica;
        actualizarMusica();
    }

    public void setMusica(boolean musica) {
        opciones.setMusica(musica);
        actualizarMusica();
    }

    public void setEfectos(boolean efectos) {
        opciones.setEfectos(efectos);
    }

    private void actualizarMusica() {
        sonarSolo(musicaMenu, pedida == Musica.MENU && opciones.isMusica());
        sonarSolo(musicaCombate, pedida == Musica.COMBATE && opciones.isMusica());
    }

    private static void sonarSolo(Music musica, boolean debeSonar) {
        if (debeSonar && !musica.isPlaying()) {
            musica.play();
        } else if (!debeSonar && musica.isPlaying()) {
            musica.stop();
        }
    }

    private static Sound cargar(String ruta) {
        return Gdx.audio.newSound(Gdx.files.internal(ruta));
    }

    @Override
    public void dispose() {
        correr.dispose();
        golpe.dispose();
        aterrizar.dispose();
        pistolazo.dispose();
        escopetazo.dispose();
        espadaEquipada.dispose();
        espadaCae.dispose();
        armaEquipada.dispose();
        boton.dispose();
        botonSeleccionado.dispose();
        musicaMenu.dispose();
        musicaCombate.dispose();
    }
}
