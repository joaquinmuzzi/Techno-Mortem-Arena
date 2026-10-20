# Política de seguridad

## Versiones con soporte

Techno Mortem Arena es un proyecto escolar chico: solo la última versión publicada (y `main`)
recibe correcciones.

## Cómo está expuesto el juego

Techno Mortem Arena es un juego de escritorio que se ejecuta en tu propia computadora. El duelo
local no abre ningún puerto. Lee y escribe un único archivo de datos, `niveles/mapas.txt`, donde el
editor de niveles guarda los mapas.

El **multijugador** usa UDP. Quien crea una sala (el anfitrión) abre un puerto UDP (7777 por
defecto) en **todas** las interfaces de su computadora, y cualquiera que conozca la dirección y el
puerto puede intentar unirse. La sala acepta un solo cliente a la vez. El juego no cifra el tráfico
ni autentica a las personas: solo evita que un tercero tome el lugar de un cliente ya conectado
(cada sesión tiene un token al azar y las entradas solo valen desde la dirección del cliente), y
descarta todo paquete con otro formato o fuera de rango. El cliente baja el mapa del anfitrión,
y lo valida (largo y caracteres permitidos) antes de usarlo.

Ejemplos de problemas que queremos conocer:

- un mapa de `niveles/mapas.txt` armado a propósito que haga que el juego lea o escriba archivos
  fuera de su carpeta, ejecute código o se caiga de una forma que afecte al sistema;
- una dependencia con una vulnerabilidad que el juego use de manera vulnerable;
- un paquete de red mal formado que cuelgue, tumbe o tome el control del anfitrión o del cliente, o
  que una sala pueda usarse para atacar a un tercero (por ejemplo, para amplificar tráfico);
- que alguien sin el token pueda mover al jugador de otra persona o sacarla de la sala;
- credenciales, claves o datos personales subidos por error al repositorio.

Fuera de alcance: tramposos en una partida local y errores de balance del juego.

## Cómo reportar una vulnerabilidad

Por favor **no abras un issue público**. Reportalo en privado a través de GitHub:
[Reportar una vulnerabilidad](https://github.com/joaquinmuzzi/Techno-Mortem-Arena/security/advisories/new)
(pestaña *Security* del repositorio, y después *Report a vulnerability*).

Incluí qué encontraste, cómo reproducirlo y qué podría hacer alguien con eso. Buscamos responder en
7 días. Cuando la corrección esté lista, se publica la versión y el aviso, mencionándote salvo que
prefieras que no.
