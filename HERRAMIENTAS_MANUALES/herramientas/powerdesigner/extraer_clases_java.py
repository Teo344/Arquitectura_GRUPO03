import sys,os,re
"""Extrae las clases de un servidor Java (paquete ec.edu.monster) y genera la especificación de un diagrama de clases
para dc_detalle.vbs: paquetes, clases con atributos y operaciones, asociaciones y dependencias.
Se omiten el paquete de pruebas, los tipos anidados privados y los campos LOG.
uso: extraer_clases_java.py <carpeta src/main/java> <salida_spec.txt>"""
raiz,salida=sys.argv[1:3]
BASE='ec.edu.monster'
# disposición: columna, orden dentro de la columna y número de subcolumnas de clases en el recuadro del paquete
DISPOSICION={'seguridad':(0,0,1),'controladores.wservicios':(1,0,1),'controladores.recursos':(1,0,2),'vista':(1,1,2),
             'servicios':(2,0,2),'datos':(3,0,1),'modelo':(4,0,1)}
OMITIR={'pruebas'}
ESTEREOTIPOS={'WebService','ApplicationScoped','WebFilter','WebFault','XmlRootElement','Path','Provider','ApplicationPath'}
ALIAS={'Path':'Resource'}   # recursos JAX-RS
VIS={'public':'+','private':'-','protected':'#'}

def limpiar(s):
    "quita comentarios y vacía bloques de texto y literales en una sola pasada (un '//' dentro de un string no es comentario)"
    return re.sub(r'"""[\s\S]*?"""|/\*[\s\S]*?\*/|//[^\n]*|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\])\'',
                  lambda m: '' if m.group(0).startswith('/') else ('""' if m.group(0)[0]=='"' else "''"),s)

def miembros(cuerpo):
    "divide el cuerpo de un tipo en (encabezado, cuerpo o None) al nivel superior"
    res=[]; i=0; ini=0; prof=0; par=0
    while i<len(cuerpo):
        c=cuerpo[i]
        if c=='(': par+=1
        elif c==')': par-=1
        elif c==';' and par==0:
            res.append((cuerpo[ini:i].strip(),None)); ini=i+1
        elif c=='{' and par==0:
            j=i; prof=0
            while True:
                if cuerpo[j]=='{': prof+=1
                elif cuerpo[j]=='}':
                    prof-=1
                    if prof==0: break
                j+=1
            res.append((cuerpo[ini:i].strip(),cuerpo[i+1:j])); i=j; ini=j+1
        i+=1
    return [m for m in res if m[0] or m[1] is not None]

def dividir(s,sep=','):
    "divide respetando <> y ()"
    partes=[]; prof=0; act=''
    for c in s:
        if c in '<(': prof+=1
        elif c in '>)': prof-=1
        if c==sep and prof==0: partes.append(act); act=''
        else: act+=c
    if act.strip(): partes.append(act)
    return [p.strip() for p in partes]

def sin_anotaciones(s):
    anot=[]
    while True:
        m=re.match(r'\s*@(\w+)(\s*\((?:[^()]|\([^()]*\))*\))?',s)
        if not m: return anot,s.strip()
        anot.append(m.group(1)); s=s[m.end():]

def var(decl):
    "tipo y nombre de 'mods Tipo nombre' (o parámetro)"
    _,decl=sin_anotaciones(decl)
    t=decl.split()
    mods=[x for x in t if x in ('public','private','protected','static','final','volatile','transient')]
    resto=' '.join(x for x in t if x not in mods)
    m=re.match(r'(.+?)\s+(\w+)$',resto)
    return mods,m.group(1).replace(' ',''),m.group(2)

TIPO=re.compile(r'^(?P<mods>(?:\w+\s+)*?)(?P<arroba>@)?(?P<tipo>class|record|enum|interface)\s+(?P<nombre>\w+)(?P<gen><[^{]*?>)?\s*(?:\((?P<comp>[^)]*)\))?(?P<resto>[^{]*)$',re.S)
clases=[]   # dict: paq, nombre, estereotipo, attrs[(vis,nombre,tipo,static,estereotipo)], ops[(vis,nombre,ret,params,est)], campos[(nombre,tipo)], cuerpo
def tipo(paq,encabezado,cuerpo,externo=None):
    anot,enc=sin_anotaciones(encabezado)
    m=TIPO.match(enc)
    mods=m.group('mods').split(); clase=m.group('tipo'); nombre=m.group('nombre')
    if externo and 'private' in mods: return
    est=[ALIAS.get(a,a) for a in anot if a in ESTEREOTIPOS]
    resto=m.group('resto')
    if m.group('arroba'): est=['annotation']
    elif clase=='record': est=['record']+est
    elif clase=='enum': est=['Enum']
    elif 'SOAPHandler' in resto: est=['SOAPHandler']
    c=dict(paq=paq,nombre=nombre,est=est[0] if est else '',interfaz=clase=='interface',attrs=[],ops=[],campos=[],
           cuerpo=cuerpo,handler='HandlerChain' in anot,protegido='Protegido' in anot)
    clases.append(c)
    if m.group('comp'):
        for p in dividir(m.group('comp')):
            _,t,n=var(p); c['attrs'].append(('+',n,t,0,'')); c['campos'].append((n,t))
    ms=miembros(cuerpo)
    if clase=='enum' and ms and ms[0][1] is None and '(' not in ms[0][0].split('{')[0][:0]:
        primero=ms[0][0]
        if re.match(r'^[A-Z_0-9]+(\s*\(|\s*,|\s*$)',primero):
            for k in dividir(primero):
                c['attrs'].append(('+',re.match(r'(\w+)',k).group(1),'',0,'EnumConstant'))
            ms=ms[1:]
    for enc2,cu in ms:
        anot2,e2=sin_anotaciones(enc2)
        if TIPO.match(e2):
            tipo(paq,enc2,cu,externo=c); continue
        if cu is not None and '(' in e2:          # método o constructor
            mods2=[]
            while True:
                mo=re.match(r'(public|private|protected|static|final|synchronized|abstract|default)\s+',e2)
                if not mo: break
                mods2.append(mo.group(1)); e2=e2[mo.end():]
            mm=re.match(r'^(?:<[^>]*>\s*)?(?:(?P<ret>[\w<>\[\],.? ]+?)\s+)?(?P<n>\w+)\s*\((?P<p>.*)\)\s*(?:throws[\w\s,.]+)?$',e2,re.S)
            if not mm: continue
            n=mm.group('n'); ret=(mm.group('ret') or '').replace(' ','')
            if n==nombre and not ret: ret=''; est2='Constructor'
            else: est2=''
            if est2=='Constructor' and clase=='enum': continue
            params=[]
            for p in dividir(mm.group('p')):
                _,t,pn=var(p); params.append((t,pn))
            vis=next((VIS[x] for x in mods2 if x in VIS),'*' if not c['interfaz'] else '+')
            c['ops'].append((vis,n,ret,params,est2,'static' in mods2))
        elif cu is None and enc2 and '(' not in e2.split('=')[0]:   # campo
            decl=e2.split('=')[0]
            try: mods2,t,n=var(decl)
            except AttributeError: continue
            if n=='LOG': continue
            vis=next((VIS[x] for x in mods2 if x in VIS),'*')
            c['attrs'].append((vis,n,t,1 if 'static' in mods2 else 0,''))
            if 'static' not in mods2: c['campos'].append((n,t))

for dp,_,fs in os.walk(os.path.join(raiz,*BASE.split('.'))):
    for f in sorted(fs):
        if not f.endswith('.java'): continue
        paq=os.path.relpath(dp,os.path.join(raiz,*BASE.split('.'))).replace(os.sep,'.')
        if paq in OMITIR: continue
        s=limpiar(open(os.path.join(dp,f),encoding='utf8').read())
        s=re.sub(r'^\s*(package|import)[^;]*;','',s,flags=re.M)
        for enc,cu in miembros(s):
            if cu is not None: tipo(paq,enc,cu)

nombres={c['nombre'] for c in clases}
def tipos_en(t): return [x for x in re.findall(r'\w+',t) if x in nombres]
asoc=[]; dep=[]; hay=set()
for c in clases:
    for n,t in c['campos']:
        for d in tipos_en(t):
            if d==c['nombre']: continue
            mult='0..*' if re.match(r'(List|Set|Map|Collection)<',t) else '1'
            asoc.append((c['nombre'],d,n,mult)); hay.add((c['nombre'],d))
    # cuerpo propio sin los tipos anidados
    propio=c['cuerpo']
    for m in re.finditer(r'(throw\s+)?new\s+(\w+)\s*\(',propio):
        d=m.group(2)
        if d in nombres and d!=c['nombre'] and (c['nombre'],d) not in hay:
            dep.append((c['nombre'],d,'throw' if m.group(1) else 'create')); hay.add((c['nombre'],d))
    for d in sorted(set(re.findall(r'\b([A-Z]\w+)\.\w+\s*\(',propio))):
        if d in nombres and d!=c['nombre'] and (c['nombre'],d) not in hay:
            dep.append((c['nombre'],d,'use')); hay.add((c['nombre'],d))
    if c['handler'] and 'ManejadorAutenticacion' in nombres:
        dep.append((c['nombre'],'ManejadorAutenticacion','HandlerChain'))
    if c['protegido'] and 'Protegido' in nombres:   # REST: enlace por nombre del filtro (@NameBinding)
        dep.append((c['nombre'],'Protegido','Protegido'))

paqs=sorted({c['paq'] for c in clases},key=lambda p:DISPOSICION.get(p,(9,0,1))[:2])
out=[]
for p in paqs:
    col,orden,sub=DISPOSICION.get(p,(5,len(out),1))
    out.append(f'PAQ|{p}|{BASE}.{p}|{col}|{orden}|{sub}')
for c in clases:
    out.append(f"CLASE|{c['paq']}|{c['nombre']}|{c['est']}|{'I' if c['interfaz'] else 'C'}")
    for vis,n,t,st,e in c['attrs']: out.append(f"ATR|{c['nombre']}|{vis}|{n}|{t}|{st}|{e}")
    for vis,n,ret,params,e,st in c['ops']:
        out.append(f"OP|{c['nombre']}|{vis}|{n}|{ret}|{';'.join(t+' '+pn for t,pn in params)}|{e}|{1 if st else 0}")
for a in asoc: out.append('ASOC|'+'|'.join(a))
for d in dep: out.append('DEP|'+'|'.join(d))
open(salida,'w',encoding='utf8').write('\n'.join(out)+'\n')
print(len(clases),'clases',len(asoc),'asociaciones',len(dep),'dependencias')
