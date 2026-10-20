package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;

/**
 * La entrada de un jugador que juega desde otra instancia: la reconstruye a partir de los paquetes
 * {@link Protocolo#ENTRADA} que van llegando.
 *
 * <p>Los estados (izquierda, derecha, estocada) se pisan con el ultimo paquete. Las acciones de una
 * sola vez llegan como contadores que solo suben: una accion es nueva cuando el contador avanzo
 * respecto del ultimo visto. Asi un paquete duplicado no repite el salto, y uno perdido se recupera
 * con el siguiente, que trae el contador actualizado.
 */
public final class EntradaRemota implements FuenteEntrada {

    private boolean izquierda;
    private boolean derecha;
    private boolean estocada;

    private int ultimaSecuencia;
    private boolean huboPaquete;
    private byte ultimoSalto;
    private byte ultimoSubir;
    private byte ultimoBajar;

    private boolean saltoPendiente;
    private boolean subirPendiente;
    private boolean bajarPendiente;

    /**
     * Incorpora un paquete de entrada.
     *
     * @return {@code false} si era mas viejo que el ultimo ya recibido y se descarto
     */
    boolean aplicar(int secuencia, boolean izquierda, boolean derecha, boolean estocada,
                    byte contadorSalto, byte contadorSubir, byte contadorBajar) {
        if (huboPaquete && !Protocolo.esMasNueva(secuencia, ultimaSecuencia)) {
            return false;
        }
        huboPaquete = true;
        ultimaSecuencia = secuencia;
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.estocada = estocada;
        saltoPendiente |= avanzo(contadorSalto, ultimoSalto);
        subirPendiente |= avanzo(contadorSubir, ultimoSubir);
        bajarPendiente |= avanzo(contadorBajar, ultimoBajar);
        ultimoSalto = contadorSalto;
        ultimoSubir = contadorSubir;
        ultimoBajar = contadorBajar;
        return true;
    }

    /** Un contador de 8 bits "avanzo" si la diferencia con el anterior es de 1 a 127 (da la vuelta en 255). */
    private static boolean avanzo(byte nuevo, byte viejo) {
        int diferencia = (nuevo - viejo) & 0xFF;
        return diferencia >= 1 && diferencia <= 127;
    }

    @Override
    public void leer(EntradaJugador destino) {
        destino.izquierda = izquierda;
        destino.derecha = derecha;
        destino.estocada = estocada;
        destino.saltar = saltoPendiente;
        destino.subirEspada = subirPendiente;
        destino.bajarEspada = bajarPendiente;
        // Las acciones de una sola vez se consumen: valen para un unico fotograma.
        saltoPendiente = false;
        subirPendiente = false;
        bajarPendiente = false;
    }
}
