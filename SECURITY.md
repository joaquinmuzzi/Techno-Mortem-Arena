# Política de seguridad

## Versiones con soporte

Techno Mortem Arena es un proyecto escolar chico: solo la última versión publicada (y `main`)
recibe correcciones.

## Cómo está expuesto el juego

Techno Mortem Arena es un juego de escritorio que se ejecuta en tu propia computadora. Hoy no abre
ningún puerto: el duelo es local, con dos jugadores en el mismo teclado. Lee y escribe un único
archivo de datos, `niveles/mapas.txt`, donde el editor de niveles guarda los mapas.

El multijugador en red local (anfitrión y cliente por sockets de Java) todavía no está implementado.
Cuando llegue, el juego va a abrir y aceptar conexiones en la red local, y esa parte va a entrar en
esta política.

Ejemplos de problemas que queremos conocer:

- un mapa de `niveles/mapas.txt` armado a propósito que haga que el juego lea o escriba archivos
  fuera de su carpeta, ejecute código o se caiga de una forma que afecte al sistema;
- una dependencia con una vulnerabilidad que el juego use de manera vulnerable;
- cuando exista el multijugador: un paquete de red mal formado que cuelgue o tome el control del
  anfitrión o del cliente, o que el juego quede accesible desde fuera de la red local;
- credenciales, claves o datos personales subidos por error al repositorio.

Fuera de alcance: tramposos en una partida local y errores de balance del juego.

## Cómo reportar una vulnerabilidad

Por favor **no abras un issue público**. Reportalo en privado a través de GitHub:
[Reportar una vulnerabilidad](https://github.com/joaquinmuzzi/Techno-Mortem-Arena/security/advisories/new)
(pestaña *Security* del repositorio, y después *Report a vulnerability*).

Incluí qué encontraste, cómo reproducirlo y qué podría hacer alguien con eso. Buscamos responder en
7 días. Cuando la corrección esté lista, se publica la versión y el aviso, mencionándote salvo que
prefieras que no.
