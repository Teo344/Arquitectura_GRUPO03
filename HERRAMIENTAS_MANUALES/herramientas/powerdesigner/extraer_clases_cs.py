import sys,os,re
"""Extrae las clases de un servidor C# (.NET, espacio de nombres ec.edu.monster) y genera la especificación de un diagrama
de clases para dc_detalle.vbs, con el mismo formato que extraer_clases_java.py (más REAL para las realizaciones).
Las propiedades se muestran como atributos. Se omiten el espacio de nombres de pruebas, los tipos anidados privados y
Properties/AssemblyInfo.
uso: extraer_clases_cs.py <carpeta del proyecto> <salida_spec.txt>"""
raiz,salida=sys.argv[1:3]
BASE='ec.edu.monster'
DISPOSICION={'seguridad':(0,0,1),'controladores.wservicios':(1,0,2),'vista':(1,1,2),
             'servicios':(2,0,2),'datos':(3,0,1),'modelo':(4,0,1)}
OMITIR={'pruebas'}
VIS={'public':'+','private':'-','protected':'#','internal':'*'}
MODS=('public','private','protected','internal','static','sealed','abstract','partial','readonly','const',
      'virtual','override','new','extern','unsafe','volatile','async')

def limpiar(s):
    "quita comentarios y vacía strings (incluidos los verbatim @\"...\" de varias líneas) en una sola pasada"
    return re.sub(r'@"(?:""|[^"])*"|/\*[\s\S]*?\*/|//[^\n]*|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\])\'',
                  lambda m: '' if m.group(0).startswith('/') else ('""' if '"' in m.group(0)[:2] else "''"),s)

def miembros(cuerpo):
    "divide un cuerpo en (encabezado, cuerpo o None) al nivel superior"
    res=[]; i=0; ini=0; par=0
    while i<len(cuerpo):
        c=cuerpo[i]
        if c in '([': par+=1
        elif c in ')]': par-=1
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
    partes=[]; prof=0; act=''
    for c in s:
        if c in '<([': prof+=1
        elif c in '>)]': prof-=1
        if c==sep and prof==0: partes.append(act); act=''
        else: act+=c
    if act.strip(): partes.append(act)
    return [p.strip() for p in partes]

def sin_atributos(s):
    "quita los atributos [..] del inicio y devuelve sus nombres (sin los [return: ...])"
    nombres=[]
    while True:
        s=s.lstrip()
        if not s.startswith('['): return nombres,s
        prof=0
        for k,c in enumerate(s):
            if c=='[': prof+=1
            elif c==']':
                prof-=1
                if prof==0: break
        dentro=s[1:k]; s=s[k+1:]
        if not dentro.startswith('return:'):
            nombres+=[re.match(r'\s*(\w+)',a).group(1) for a in dividir(dentro)]

def separar_mods(s):
    mods=[]
    while True:
        m=re.match(r'(\w+)\s+',s)
        if not m or m.group(1) not in MODS: return mods,s
        mods.append(m.group(1)); s=s[m.end():]

def var(decl):
    "tipo y nombre de 'mods Tipo nombre [= valor]' o de un parámetro"
    _,decl=sin_atributos(decl.split('=')[0])
    mods,decl=separar_mods(decl.strip())
    decl=re.sub(r'^(ref|out|in|params|this)\s+','',decl)
    m=re.match(r'(.+?)\s+(\w+)$',decl.strip())
    return mods,m.group(1).replace(' ',''),m.group(2)

TIPO=re.compile(r'^(?P<tipo>class|interface|enum|struct)\s+(?P<nombre>\w+)(?P<gen><[^>]*>)?\s*(?::\s*(?P<bases>[^{]*))?$',re.S)
clases=[]
def tipo(paq,encabezado,cuerpo,externo=None):
    atrs,enc=sin_atributos(encabezado)
    mods,enc=separar_mods(enc)
    m=TIPO.match(enc.strip())
    if externo and 'private' in mods: return
    clase=m.group('tipo'); nombre=m.group('nombre')
    bases=[b.strip() for b in dividir(m.group('bases') or '')]
    est=''
    if 'ServiceContract' in atrs: est='ServiceContract'
    elif 'DataContract' in atrs: est='DataContract'
    elif clase=='enum': est='Enum'
    elif any(b=='Attribute' for b in bases): est='Attribute'
    elif any(b.startswith('IDispatchMessageInspector') for b in bases): est='MessageInspector'
    elif any(b.startswith('FaultException') for b in bases): est='FaultException'
    elif 'static' in mods: est='static'
    c=dict(paq=paq,nombre=nombre,est=est,interfaz=clase=='interface',attrs=[],ops=[],campos=[],cuerpo=cuerpo,
           bases=bases,atrs_clase=atrs)
    clases.append(c)
    for enc2,cu in miembros(cuerpo):
        atrs2,e2=sin_atributos(enc2)
        mods2,e3=separar_mods(e2)
        if TIPO.match(e3.strip()):
            tipo(paq,enc2,cu,externo=c); continue
        if not e3 or e3.startswith('='): continue
        vis=next((VIS[x] for x in mods2 if x in VIS),'+' if c['interfaz'] else '-')
        estatico='static' in mods2 or 'const' in mods2
        flecha=e3.find('=>'); paren=e3.find('(')
        es_metodo=paren>=0 and (flecha<0 or paren<flecha) and (cu is not None or flecha>=0 or c['interfaz'])
        if es_metodo:
            antes=e3[:paren].strip(); prof=0
            for k in range(paren,len(e3)):
                if e3[k]=='(': prof+=1
                elif e3[k]==')':
                    prof-=1
                    if prof==0: break
            params_txt=e3[paren+1:k]
            antes=re.sub(r'<[^<>]*>$','',antes)                     # nombre genérico EnTransaccion<T>
            mm=re.match(r'^(?:(?P<ret>.+?)\s+)?(?P<n>\w+)$',antes)
            if not mm: continue
            n=mm.group('n'); ret=(mm.group('ret') or '').replace(' ','')
            est2='Constructor' if n==nombre and not ret else ''
            params=[]
            for p in dividir(params_txt):
                _,t,pn=var(p); params.append((t,pn))
            c['ops'].append((vis,n,ret,params,est2,estatico))
        elif cu is not None and re.search(r'\b(get|set|init)\b',cu) and paren<0:   # propiedad
            _,t,n=var(e3)
            c['attrs'].append((vis,n,t,1 if estatico else 0,''))
            if not estatico: c['campos'].append((n,t))
        elif cu is None and paren<0 or (cu is None and '=' in e3 and e3.find('=')<paren):   # campo o constante
            try: _,t,n=var(e3)
            except AttributeError: continue
            if flecha>=0: n=re.match(r'.*?(\w+)\s*$',e3[:flecha]).group(1)
            c['attrs'].append((vis,n,t,1 if estatico else 0,''))
            if not estatico: c['campos'].append((n,t))

for dp,_,fs in os.walk(os.path.join(raiz,*BASE.split('.'))):
    for f in sorted(fs):
        if not f.endswith('.cs'): continue
        paq=os.path.relpath(dp,os.path.join(raiz,*BASE.split('.'))).replace(os.sep,'.')
        if paq in OMITIR: continue
        s=limpiar(open(os.path.join(dp,f),encoding='utf-8-sig').read())
        s=re.sub(r'^\s*using\s+(static\s+)?[\w.]+(\s*=\s*[\w.<>]+)?\s*;','',s,flags=re.M)   # solo directivas
        for enc,cu in miembros(s):
            if enc.startswith('namespace'):
                for enc2,cu2 in miembros(cu):
                    if cu2 is not None: tipo(paq,enc2,cu2)

nombres={c['nombre'] for c in clases}
interfaces={c['nombre'] for c in clases if c['interfaz']}
def tipos_en(t): return [x for x in re.findall(r'\w+',t) if x in nombres]
asoc=[]; dep=[]; real=[]; hay=set()
for c in clases:
    for b in c['bases']:
        b=re.match(r'\w+',b).group(0)
        if b in interfaces:
            real.append((c['nombre'],b)); hay.add((c['nombre'],b))
            if c['est']=='' : c['est']='WebService'
    for n,t in c['campos']:
        for d in tipos_en(t):
            if d==c['nombre'] or (c['nombre'],d) in hay: continue
            mult='0..*' if re.match(r'(List|IList|IEnumerable|ICollection|Dictionary|ConcurrentDictionary)<',t) else '1'
            asoc.append((c['nombre'],d,n,mult)); hay.add((c['nombre'],d))
    if 'Protegido' in c['atrs_clase'] and 'ProtegidoAttribute' in nombres:
        dep.append((c['nombre'],'ProtegidoAttribute','Protegido')); hay.add((c['nombre'],'ProtegidoAttribute'))
    propio=c['cuerpo']
    for m in re.finditer(r'(throw\s+)?new\s+(\w+)\s*[({]',propio):
        d=m.group(2)
        if d in nombres and d!=c['nombre'] and (c['nombre'],d) not in hay:
            dep.append((c['nombre'],d,'throw' if m.group(1) else 'create')); hay.add((c['nombre'],d))
    for d in sorted(set(re.findall(r'\b([A-Z]\w+)\.(?:\w+\s*\(|Instancia\b)',propio))):
        if d in nombres and d!=c['nombre'] and (c['nombre'],d) not in hay:
            dep.append((c['nombre'],d,'use')); hay.add((c['nombre'],d))

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
for r in real: out.append('REAL|'+'|'.join(r))
for d in dep: out.append('DEP|'+'|'.join(d))
open(salida,'w',encoding='utf8').write('\n'.join(out)+'\n')
print(len(clases),'clases',len(asoc),'asociaciones',len(real),'realizaciones',len(dep),'dependencias')
