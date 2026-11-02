import sys,re,uuid
"""Ajusta un diagrama de arquitectura generado por ea_detalle.vbs desde la plantilla de TI.1.1:
- anida cada símbolo dentro del símbolo de su área (<c:SubSymbols>), como en la plantilla; sin eso los nombres de las
  áreas no se muestran arriba y las áreas tapan los enlaces;
- deja los enlaces al final para que se dibujen encima;
- cambia los dos textos del título (TextSymbol.Text no se puede escribir por COM) y los centra sobre el diagrama;
- asigna GUID nuevos a los objetos heredados.
uso: finalizar_ea.py <eam> <plantilla_eam> <spec.txt>"""
nuevo,plantilla,spec=sys.argv[1:4]
x=open(nuevo,encoding='utf8').read(); v=open(plantilla,encoding='utf8').read()
lineas=[l.split('|') for l in open(spec,encoding='utf8').read().splitlines()]

# ---------- título
titulo,subtitulo=next(l[1:3] for l in lineas if l[0]=='TITULO')
areas=[l for l in lineas if l[0]=='AREA' and l[3]=='-']
izq=min(int(a[4]) for a in areas); der=max(int(a[6]) for a in areas); arriba=max(int(a[5]) for a in areas)
textos=list(re.finditer(r'<o:TextSymbol Id="[^"]+">.*?</o:TextSymbol>',x,re.S))
assert len(textos)==2, 'la plantilla debe tener los dos textos del título'
for m,(t,dy,alto) in reversed(list(zip(textos,[(titulo,7000,3400),(subtitulo,3200,3000)]))):
    b=m.group(0)
    b=re.sub(r'<a:Text>[^<]*</a:Text>',lambda r:f'<a:Text>{t}</a:Text>',b)
    b=re.sub(r'<a:Rect>[^<]*</a:Rect>',lambda r:f'<a:Rect>(({izq},{arriba+dy-alto}), ({der},{arriba+dy}))</a:Rect>',b)
    x=x[:m.start()]+b+x[m.end():]

# ---------- anidar los símbolos en sus áreas
padre={l[1]:l[3] for l in lineas if l[0] in ('AREA','APP','SRV','COMP','BD')}
codigo={m.group(1):m.group(2) for m in re.finditer(
    r'<o:(?:ArchitectureArea|EnterpriseApplication|ApplicationService|Component|Database) Id="([^"]+)">.*?<a:Code>([^<]*)</a:Code>',x,re.S)}
d0=x.index('<c:Symbols>',x.index('<o:ApplicationArchitectureDiagram Id='))+len('<c:Symbols>')
d1=x.index('</c:Symbols>',d0)
sims=[m.group(0) for m in re.finditer(r'<o:(\w+Symbol) Id="[^"]+">.*?</o:\1>',x[d0:d1],re.S)]
def cod_de(b):
    r=re.search(r'<c:Object>\s*<o:\w+ Ref="([^"]+)"/>',b)
    return codigo.get(r.group(1)) if r else None
porcod={cod_de(b):b for b in sims if cod_de(b)}
def armar(c):
    b=porcod[c]; hijos=[h for h,pa in padre.items() if pa==c and h in porcod]
    if hijos:
        b=b.replace('<c:Object>','<c:SubSymbols>\n'+''.join(armar(h) for h in hijos)+'</c:SubSymbols>\n<c:Object>',1)
    return b+'\n'
textos=[b+'\n' for b in sims if b.startswith('<o:TextSymbol')]
enlaces=[b+'\n' for b in sims if b.startswith('<o:ApplicationLinkSymbol')]
raices=[c for c,pa in padre.items() if pa=='-' and c in porcod]
assert len(textos)+len(enlaces)+len(porcod)==len(sims), 'hay símbolos sin clasificar'
x=x[:d0]+'\n'+''.join(textos)+''.join(armar(c) for c in raices)+''.join(enlaces)+x[d1:]

# ---------- GUID nuevos
pat=r'[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}'
viejos=set(re.findall(pat,v)); n=0
for gu in set(re.findall(pat,x)) & viejos:
    if re.search(r'(<a:ObjectID>|ID="\{)'+gu,x):
        x=x.replace(gu,str(uuid.uuid4()).upper()); n+=1
open(nuevo,'w',encoding='utf8').write(x)
m=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',x)
print('símbolos',len(sims),'guid reemplazados',n,'ModelID',m.group(1))
