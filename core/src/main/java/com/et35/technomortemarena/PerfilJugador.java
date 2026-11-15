package com.et35.technomortemarena;

import com.badlogic.gdx.graphics.Color;

public final class PerfilJugador {
    public interface Almacen {
        String leer(String clave, String porDefecto);

        void guardar(String clave, String valor);
    }

    public static final int LARGO_MAXIMO = 12;

    static final String CLAVE_NOMBRE = "nombre";
    static final String CLAVE_COLOR = "color";

    public static final Color[] PALETA = {
        new Color(0.42f, 0.78f, 1f, 1f),
        new Color(1f, 0.48f, 0.42f, 1f),
        new Color(0.45f, 0.90f, 0.45f, 1f),
        new Color(1f, 0.85f, 0.30f, 1f),
        new Color(0.72f, 0.52f, 1f, 1f),
        new Color(1f, 0.62f, 0.22f, 1f),
        new Color(1f, 0.50f, 0.80f, 1f),
        new Color(0.90f, 0.92f, 0.95f, 1f),
    };
    public static final String[] NOMBRES_COLORES = {
        "AZUL", "ROJO", "VERDE", "AMARILLO", "VIOLETA", "NARANJA", "ROSA", "BLANCO",
    };
    public static final int COLOR_UNO = 0;
    public static final int COLOR_DOS = 1;

    private final Almacen almacen;
    private String nombre;
    private int color;

    public PerfilJugador(Almacen almacen) {
        this.almacen = almacen;
        nombre = limpiar(almacen.leer(CLAVE_NOMBRE, ""));
        color = COLOR_UNO;
        try {
            color = colorValido(Integer.parseInt(almacen.leer(CLAVE_COLOR, String.valueOf(COLOR_UNO)).trim()), COLOR_UNO);
        } catch (NumberFormatException e) {
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = colorValido(color, COLOR_UNO);
        almacen.guardar(CLAVE_COLOR, String.valueOf(this.color));
    }

    public static int colorValido(int color, int porDefecto) {
        return color >= 0 && color < PALETA.length ? color : porDefecto;
    }

    public static int colorDistinto(int elegido, int ocupado) {
        if (elegido != ocupado) {
            return elegido;
        }
        return ocupado == COLOR_DOS ? COLOR_UNO : COLOR_DOS;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = limpiar(nombre);
        almacen.guardar(CLAVE_NOMBRE, this.nombre);
    }

    public static boolean esCaracterValido(char c) {
        char mayuscula = Character.toUpperCase(c);
        return (mayuscula >= 'A' && mayuscula <= 'Z') || (c >= '0' && c <= '9') || mayuscula == 'Ñ'
            || c == ' ' || c == '-';
    }

    public static String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder limpio = new StringBuilder();
        for (int i = 0; i < texto.length() && limpio.length() < LARGO_MAXIMO; i++) {
            char c = texto.charAt(i);
            if (!esCaracterValido(c)) {
                continue;
            }
            if (c == ' ' && (limpio.length() == 0 || limpio.charAt(limpio.length() - 1) == ' ')) {
                continue;
            }
            limpio.append(Character.toUpperCase(c));
        }
        return limpio.toString().trim();
    }

    public static String mostrar(String nombre, String porDefecto) {
        String limpio = limpiar(nombre);
        return limpio.isEmpty() ? porDefecto : limpio;
    }
}
