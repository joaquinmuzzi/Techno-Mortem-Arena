package com.et35.technomortemarena.partida;

public enum PiezaNivel {
    FILA_DE_PINCHOS("FILA DE PINCHOS", "XXXXXX", "XXXXXX"),
    TRAMPOLIN("TRAMPOLIN", "TTTT", "TTTT"),
    BLOQUE("BLOQUE", "########", "########", "########", "########", "########", "########", "########", "########"),
    RECTANGULO("RECTANGULO", "##########", "##########", "##########", "##########"),
    ESQUINA("ESQUINA", "##..", "##..", "##..", "##..", "##..", "##..", "##..", "##..", "####", "####"),
    CINTA_IZQUIERDA("CINTA IZQUIERDA", "LLLLLL", "LLLLLL"),
    CINTA_DERECHA("CINTA DERECHA", "RRRRRR", "RRRRRR"),
    PINCHOS_CON_CINTAS("PINCHOS CON CINTAS", "RRRRRRXXXXLLLLLL", "RRRRRRXXXXLLLLLL"),
    SPAWNER_PISTOLA("SPAWN PISTOLA", "PPP", "PPP", "PPP"),
    SPAWNER_ESCOPETA("SPAWN ESCOPETA", "EEE", "EEE", "EEE"),
    SPAWNER_RIFLE_LASER("SPAWN RIFLE LASER", "FFF", "FFF", "FFF"),
    SPAWNER_LANZACOHETES("SPAWN COHETES", "CCC", "CCC", "CCC");

    private final String nombre;
    private final String[] filas;

    PiezaNivel(String nombre, String... filas) {
        this.nombre = nombre;
        this.filas = filas;
    }

    public String getNombre() {
        return nombre;
    }

    public int getAncho() {
        return filas[0].length();
    }

    public int getAlto() {
        return filas.length;
    }

    public char celda(int dx, int dy) {
        if (dx < 0 || dx >= getAncho() || dy < 0 || dy >= getAlto()) {
            return '.';
        }
        return filas[getAlto() - 1 - dy].charAt(dx);
    }

    public static PiezaNivel porIndice(int indice) {
        PiezaNivel[] todas = values();
        return indice >= 0 && indice < todas.length ? todas[indice] : null;
    }
}
