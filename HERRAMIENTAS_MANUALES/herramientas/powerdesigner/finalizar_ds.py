import sys,re,uuid
"""Ajusta un diagrama de secuencia generado por ds_detalle.vbs desde la plantilla de TI.1.1:
- agrega las activaciones (se abren con una llamada y se cierran con el retorno), porque no se pueden crear por COM;
- conecta los mensajes a esas activaciones y alarga las líneas de vida hasta el último mensaje;
- asigna GUID nuevos a los objetos heredados de la plantilla.
uso: finalizar_ds.py <oom> <plantilla_oom>"""
nuevo,plantilla=sys.argv[1:3]
x=open(nuevo,encoding='utf8').read(); v=open(plantilla,encoding='utf8').read()
sig=max(int(i) for i in re.findall(r'Id="o(\d+)"',x))+1

def puntos(s): return [tuple(map(int,p)) for p in re.findall(r'\((-?\d+),(-?\d+)\)',s)]

# PowerDesigner reacomoda las líneas de vida al dibujarlas: los extremos de cada mensaje se recalculan con la x real
# de su línea de vida (al borde de la activación, salvo en el actor) y los reflexivos usan un lazo ancho para el texto.
vida={}; actor=set()
for s in re.finditer(r'<o:(UMLObjectSequenceSymbol|ActorSequenceSymbol) Id="([^"]+)">.*?<o:LifelineSymbol Id="[^"]+">\s*<a:Rect>[^<]*</a:Rect>\s*<a:ListOfPoints>\(\((-?\d+),',x,re.S):
    vida[s.group(2)]=int(s.group(3))
    if s.group(1)=='ActorSequenceSymbol': actor.add(s.group(2))
def ajustar_mensaje(m):
    b=m.group(0)
    so=re.search(r'<c:SourceSymbol>\s*<o:\w+ Ref="([^"]+)"/>',b).group(1)
    de=re.search(r'<c:DestinationSymbol>\s*<o:\w+ Ref="([^"]+)"/>',b).group(1)
    pts=puntos(re.search(r'<a:ListOfPoints>([^<]*)',b).group(1)); y=pts[0][1]
    xo,xd=vida[so],vida[de]
    if so==de:
        y2=pts[-1][1]; nuevos=[(xo+450,y),(xo+13000,y),(xo+13000,y2),(xo+450,y2)]
        rect=((xo+450,y+400),(xo+13400,y2-400))
    else:
        d=1 if xd>xo else -1
        if so not in actor: xo+=450*d
        if de not in actor: xd-=450*d
        nuevos=[(xo,y),(xd,y)]; rect=((min(xo,xd),y+2200),(max(xo,xd),y-600))
    lp='('+','.join(f'({a},{c})' for a,c in nuevos)+')'
    b=re.sub(r'<a:ListOfPoints>[^<]*',lambda r:'<a:ListOfPoints>'+lp,b,count=1)
    b=re.sub(r'<a:Rect>[^<]*',lambda r:f'<a:Rect>(({rect[0][0]},{rect[0][1]}), ({rect[1][0]},{rect[1][1]}))',b,count=1)
    return b
x=re.sub(r'<o:MessageSymbol Id="[^"]+">.*?</o:MessageSymbol>',ajustar_mensaje,x,flags=re.S)

# Activaciones: un objeto se activa con el primer mensaje en que participa y se desactiva cuando devuelve el control
# (envía un retorno); la siguiente llamada que reciba abre otra activación.
flujo=dict(re.findall(r'<o:Message Id="([^"]+)">.*?<a:ControlFlow>(\w)</a:ControlFlow>',x,re.S))
msjs=[]
for ms in re.finditer(r'<o:MessageSymbol Id="([^"]+)">.*?</o:MessageSymbol>',x,re.S):
    b=ms.group(0); pts=puntos(re.search(r'<a:ListOfPoints>([^<]*)',b).group(1))
    so=re.search(r'<c:SourceSymbol>\s*<o:\w+ Ref="([^"]+)"/>',b).group(1)
    de=re.search(r'<c:DestinationSymbol>\s*<o:\w+ Ref="([^"]+)"/>',b).group(1)
    f=flujo.get(re.search(r'<c:Object>\s*<o:Message Ref="([^"]+)"/>',b).group(1),'C')
    msjs.append((ms.group(1),so,de,f,pts[0][1],min(p[1] for p in pts)))
msjs.sort(key=lambda m:-m[4])
fondo=min(m[5] for m in msjs)-3000

tramos={}; asignado={}
for sid in vida:
    if sid in actor: continue
    abierto=None
    for mid,so,de,f,y,yb in msjs:
        if sid not in (so,de): continue
        if abierto is None:
            abierto=[y,yb]; tramos.setdefault(sid,[]).append(abierto)
        abierto[1]=min(abierto[1],yb)
        n=len(tramos[sid])-1
        if so==sid: asignado[(mid,'SourceSymbol')]=(sid,n)
        if de==sid: asignado[(mid,'DestinationSymbol')]=(sid,n)
        if so==sid and de!=sid and f=='R': abierto=None

act={}
def poner_activaciones(m):
    global sig
    bloque=m.group(0); sid=m.group(1); a=''
    for n,(arriba,abajo) in enumerate(tramos.get(sid,[])):
        aid=f'o{sig}'; sig+=1; act[(sid,n)]=aid; cx=vida[sid]
        a+=(f'<o:ActivationSymbol Id="{aid}">\n<a:Rect>(({cx-450},{arriba+400}), ({cx+450},{abajo-400}))</a:Rect>\n'
            '<a:LineColor>0</a:LineColor>\n<a:FillColor>16777215</a:FillColor>\n<a:ShadowColor>16777215</a:ShadowColor>\n'
            '</o:ActivationSymbol>\n')
    return bloque.replace('</o:LifelineSymbol>\n','</o:LifelineSymbol>\n'+a,1)
x=re.sub(r'<o:UMLObjectSequenceSymbol Id="([^"]+)">.*?</o:UMLObjectSequenceSymbol>',poner_activaciones,x,flags=re.S)

def reconectar(m):
    b=m.group(0); mid=m.group(1)
    for rol in ('SourceSymbol','DestinationSymbol'):
        if (mid,rol) in asignado:
            b=re.sub(r'(<c:'+rol+r'>\s*)<o:UMLObjectSequenceSymbol Ref="[^"]+"/>',
                     lambda r: r.group(1)+f'<o:ActivationSymbol Ref="{act[asignado[(mid,rol)]]}"/>',b)
    return b
x=re.sub(r'<o:MessageSymbol Id="([^"]+)">.*?</o:MessageSymbol>',reconectar,x,flags=re.S)

# líneas de vida hasta debajo del último mensaje
def alargar(m):
    b=m.group(0)
    b=re.sub(r'(<a:Rect>\(\(-?\d+,)-?\d+(\))',lambda r: f'{r.group(1)}{fondo-400}{r.group(2)}',b,count=1)
    b=re.sub(r'(<a:ListOfPoints>\(\(-?\d+,-?\d+\),\(-?\d+,)-?\d+',lambda r: f'{r.group(1)}{fondo}',b,count=1)
    return b
x=re.sub(r'<o:LifelineSymbol Id="[^"]+">.*?</o:LifelineSymbol>',alargar,x,flags=re.S)

pat=r'[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}'
# marco de la interacción: hasta después del lazo reflexivo de la última línea de vida y del último mensaje
def marco(m):
    izq,arriba=map(int,re.match(r'\(\((-?\d+),(-?\d+)\)',m.group(2)).groups())
    return f'{m.group(1)}<a:Rect>(({izq},{arriba}), ({max(vida.values())+22000},{fondo-1500}))</a:Rect>'
x=re.sub(r'(<o:InteractionSymbol Id="[^"]+">\s*)<a:Rect>([^<]*)</a:Rect>',marco,x)

viejos=set(re.findall(pat,v)); n=0
for gu in set(re.findall(pat,x)) & viejos:
    if re.search(r'(<a:ObjectID>|ID="\{)'+gu,x):
        x=x.replace(gu,str(uuid.uuid4()).upper()); n+=1
open(nuevo,'w',encoding='utf8').write(x)
m=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',x)
print('activaciones',len(act),'guid reemplazados',n,'ModelID',m.group(1))
