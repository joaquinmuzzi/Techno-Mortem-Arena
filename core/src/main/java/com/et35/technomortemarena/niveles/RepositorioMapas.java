package com.et35.technomortemarena.niveles;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.et35.technomortemarena.mundo.Arena;

/**
 * Guarda todos los mapas del editor en un unico archivo de texto, {@code niveles/mapas.txt}.
 *
 * <p>Cada mapa ocupa un encabezado {@code [nombre]} seguido de sus filas, con el mismo formato que
 * acepta {@link Arena#desdeGrilla}. Guardar un mapa con un nombre existente lo reemplaza.
 */
public final class RepositorioMapas {

    private static final String ARCHIVO = "niveles/mapas.txt";

    private RepositorioMapas() {
    }

    public static List<MapaGuardado> cargarTodos() {
        List<MapaGuardado> mapas = new ArrayList<>();
        FileHandle archivo = Gdx.files.local(ARCHIVO);
        if (!archivo.exists()) {
            return mapas;
        }
        String[] lineas = archivo.readString().split("\\r?\\n");
        int i = 0;
        while (i < lineas.length) {
            String encabezado = lineas[i].trim();
            if (encabezado.startsWith("[") && encabezado.endsWith("]") && i + Arena.FILAS < lineas.length) {
                String[] filas = new String[Arena.FILAS];
                boolean valido = true;
                for (int k = 0; k < Arena.FILAS; k++) {
                    filas[k] = lineas[i + 1 + k];
                    valido &= filas[k].length() == Arena.COLUMNAS;
                }
                if (valido) {
                    String nombre = encabezado.substring(1, encabezado.length() - 1);
                    mapas.add(new MapaGuardado(nombre, filas));
                    i += Arena.FILAS + 1;
                    continue;
                }
            }
            i++;
        }
        return mapas;
    }

    /** Guarda el mapa, reemplazando el que tenga el mismo nombre si ya existe. */
    public static void guardar(MapaGuardado mapa) {
        List<MapaGuardado> mapas = cargarTodos();
        boolean reemplazado = false;
        for (int i = 0; i < mapas.size(); i++) {
            if (mapas.get(i).getNombre().equals(mapa.getNombre())) {
                mapas.set(i, mapa);
                reemplazado = true;
            }
        }
        if (!reemplazado) {
            mapas.add(mapa);
        }
        escribir(mapas);
    }

    private static void escribir(List<MapaGuardado> mapas) {
        StringBuilder texto = new StringBuilder("# Mapas guardados por el editor de niveles\n");
        for (MapaGuardado mapa : mapas) {
            texto.append('[').append(mapa.getNombre()).append("]\n");
            for (String fila : mapa.getFilas()) {
                texto.append(fila).append('\n');
            }
        }
        FileHandle archivo = Gdx.files.local(ARCHIVO);
        archivo.parent().mkdirs();
        archivo.writeString(texto.toString(), false);
    }
}
