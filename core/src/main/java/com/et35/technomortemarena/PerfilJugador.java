package com.et35.technomortemarena;

public final class PerfilJugador {
    public interface Almacen {
        String leer(String clave, String porDefecto);

        void guardar(String clave, String valor);
    }

    public static final int LARGO_MAXIMO = 12;

    static final String CLAVE_NOMBRE = "nombre";

    private final Almacen almacen;
    private String nombre;

    public PerfilJugador(Almacen almacen) {
        this.almacen = almacen;
        nombre = limpiar(almacen.leer(CLAVE_NOMBRE, ""));
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
