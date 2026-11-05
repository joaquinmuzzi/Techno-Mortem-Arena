package com.et35.technomortemarena.partida;

/**
 * Los objetos que el perdedor de una ronda puede colocar en el nivel. Cada uno es una forma sobre la
 * grilla de la arena, escrita con los mismos caracteres que los mapas ({@code #} solido, {@code T}
 * trampolin, {@code L} y {@code R} cintas, {@code X} pinchos y {@code .} aire).
 *
 * <p>Las medidas son en celdas: el largo es horizontal y el ancho (el grosor) es vertical. Es logica pura,
 * sin libGDX, igual que el resto del paquete.
 */
public enum PiezaNivel {

    FILA_DE_PINCHOS("FILA DE PINCHOS", "XXXXXX", "XXXXXX"),
    TRAMPOLIN("TRAMPOLIN", "TTTT", "TTTT"),
    BLOQUE("BLOQUE", "########", "########", "########", "########", "########", "########", "########", "########"),
    RECTANGULO("RECTANGULO", "##########", "##########", "##########", "##########"),
    /** Una L: 10 de alto con el brazo vertical de 2 de ancho, y 4 de largo abajo con 2 de grosor. */
    ESQUINA("ESQUINA", "##..", "##..", "##..", "##..", "##..", "##..", "##..", "##..", "####", "####"),
    CINTA_IZQUIERDA("CINTA IZQUIERDA", "LLLLLL", "LLLLLL"),
    CINTA_DERECHA("CINTA DERECHA", "RRRRRR", "RRRRRR"),
    /** Dos cintas de 6 que empujan hacia el centro, donde hay 4 de pinchos: 16 de largo y 2 de grosor. */
    PINCHOS_CON_CINTAS("PINCHOS CON CINTAS", "RRRRRRXXXXLLLLLL", "RRRRRRXXXXLLLLLL"),
    /** Bloque de 3x3 que hace aparecer una pistola encima cada tanto. */
    SPAWNER_PISTOLA("SPAWN PISTOLA", "PPP", "PPP", "PPP"),
    /** Bloque de 3x3 que hace aparecer una escopeta encima cada tanto. */
    SPAWNER_ESCOPETA("SPAWN ESCOPETA", "EEE", "EEE", "EEE");

    private final String nombre;
    /** La forma, la primera fila es la de arriba. */
    private final String[] filas;

    PiezaNivel(String nombre, String... filas) {
        this.nombre = nombre;
        this.filas = filas;
    }

    public String getNombre() {
        return nombre;
    }

    /** Largo en celdas (horizontal). */
    public int getAncho() {
        return filas[0].length();
    }

    /** Alto en celdas (vertical). */
    public int getAlto() {
        return filas.length;
    }

    /**
     * El caracter de la celda {@code (dx, dy)} de la forma, con {@code dy} contado desde abajo, o
     * {@code '.'} si queda fuera de la forma o vacia.
     */
    public char celda(int dx, int dy) {
        if (dx < 0 || dx >= getAncho() || dy < 0 || dy >= getAlto()) {
            return '.';
        }
        return filas[getAlto() - 1 - dy].charAt(dx);
    }

    /** La pieza con ese numero, o {@code null} si no existe. Es el numero que viaja por la red. */
    public static PiezaNivel porIndice(int indice) {
        PiezaNivel[] todas = values();
        return indice >= 0 && indice < todas.length ? todas[indice] : null;
    }
}
