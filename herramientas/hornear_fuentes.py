import os
import sys

from PIL import Image, ImageDraw, ImageFont

TAMANIOS = [12, 14, 16, 18, 20, 24, 28, 32, 40, 48]
ALTO_H_DE_15 = 11
CARACTERES = [chr(c) for c in range(32, 127)] + list("áéíóúÁÉÍÓÚñÑüÜ¿¡°×·")
ANCHO_TEXTURA = 512
SEPARACION = 2
FUENTES = [
    ("Orbitron.ttf", "titulo", 700),
    ("ShareTechMono.ttf", "mono", None),
]

def cargar(ruta, tamanio, peso):
    fuente = ImageFont.truetype(ruta, tamanio)
    if peso is not None:
        fuente.set_variation_by_axes([peso])
    return fuente

def tamanio_para(ruta, peso, objetivo):
    alto_h = round(ALTO_H_DE_15 * objetivo / 15)
    mejor = None
    for tamanio in range(6, 200):
        caja = cargar(ruta, tamanio, peso).getbbox("H", anchor="ls")
        error = abs((caja[3] - caja[1]) - alto_h)
        if mejor is None or error < mejor[0]:
            mejor = (error, tamanio)
    return mejor[1]

def hornear(ruta, nombre, peso, objetivo, salida):
    fuente = cargar(ruta, tamanio_para(ruta, peso, objetivo), peso)
    ascenso, descenso = fuente.getmetrics()
    glifos = []
    for caracter in CARACTERES:
        if caracter != " " and fuente.getmask(caracter).getbbox() is None:
            continue
        x0, y0, x1, y1 = fuente.getbbox(caracter, anchor="ls")
        ancho, alto = max(0, x1 - x0), max(0, y1 - y0)
        imagen = Image.new("L", (max(1, ancho), max(1, alto)), 0)
        dibujo = ImageDraw.Draw(imagen)
        dibujo.fontmode = "1"
        dibujo.text((-x0, -y0), caracter, font=fuente, anchor="ls", fill=255)
        glifos.append((caracter, imagen, ancho, alto, x0, ascenso + y0, round(fuente.getlength(caracter))))

    x = y = alto_fila = 0
    lugares = []
    for _, imagen, ancho, alto, *_ in glifos:
        if x + ancho + SEPARACION > ANCHO_TEXTURA:
            x = 0
            y += alto_fila + SEPARACION
            alto_fila = 0
        lugares.append((x, y))
        x += ancho + SEPARACION
        alto_fila = max(alto_fila, alto)
    alto_textura = 1
    while alto_textura < y + alto_fila + SEPARACION:
        alto_textura *= 2

    textura = Image.new("RGBA", (ANCHO_TEXTURA, alto_textura), (255, 255, 255, 0))
    for (caracter, imagen, ancho, alto, *_), (gx, gy) in zip(glifos, lugares):
        if ancho and alto:
            blanco = Image.new("RGBA", imagen.size, (255, 255, 255, 255))
            textura.paste(blanco, (gx, gy), imagen.point(lambda v: 255 if v >= 128 else 0))

    base = f"{nombre}-{objetivo}"
    textura.save(os.path.join(salida, base + ".png"))
    lineas = [
        f'info face="{base}" size={objetivo} bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=0 aa=1 '
        f"padding=0,0,0,0 spacing={SEPARACION},{SEPARACION}",
        f"common lineHeight={ascenso + descenso} base={ascenso} scaleW={ANCHO_TEXTURA} scaleH={alto_textura} "
        f"pages=1 packed=0",
        f'page id=0 file="{base}.png"',
        f"chars count={len(glifos)}",
    ]
    for (caracter, _, ancho, alto, dx, dy, avance), (gx, gy) in zip(glifos, lugares):
        lineas.append(f"char id={ord(caracter)} x={gx} y={gy} width={ancho} height={alto} xoffset={dx} "
                      f"yoffset={dy} xadvance={avance} page=0 chnl=15")
    with open(os.path.join(salida, base + ".fnt"), "w", encoding="utf-8", newline="\n") as archivo:
        archivo.write("\n".join(lineas) + "\n")

def main():
    origen = sys.argv[1]
    salida = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "assets", "fuentes")
    os.makedirs(salida, exist_ok=True)
    for archivo, nombre, peso in FUENTES:
        for objetivo in TAMANIOS:
            hornear(os.path.join(origen, archivo), nombre, peso, objetivo, salida)
    print("listo")

if __name__ == "__main__":
    main()
