import sys,re
"""Recalcula el <a:Rect> de cada enlace (referencias y relaciones) a partir de sus puntos: al mover las tablas por COM,
algunos enlaces verticales quedan con un rectángulo viejo y la imagen exportada sale con espacio vacío arriba.
uso: finalizar_mer.py <modelo.cdm|.ldm|.pdm> [...]"""
MARGEN = 400

def ajustar(m):
    bloque = m.group(0)
    pts = re.search(r'<a:ListOfPoints>\(([^<]*)\)</a:ListOfPoints>', bloque)
    if not pts:
        return bloque
    xy = [tuple(map(int, p.split(','))) for p in re.findall(r'\((-?\d+,-?\d+)\)', pts.group(1))]
    xs, ys = [p[0] for p in xy], [p[1] for p in xy]
    rect = f'<a:Rect>(({min(xs)-MARGEN},{max(ys)+MARGEN}), ({max(xs)+MARGEN},{min(ys)-MARGEN}))</a:Rect>'
    return re.sub(r'<a:Rect>[^<]*</a:Rect>', rect, bloque, count=1)

for ruta in sys.argv[1:]:
    x = open(ruta, encoding='utf8', newline='').read()
    y = re.sub(r'<o:(ReferenceSymbol|RelationshipSymbol) Id=.*?</o:\1>', ajustar, x, flags=re.S)
    open(ruta, 'w', encoding='utf8', newline='').write(y)
