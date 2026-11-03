import sys,math
"""Verifica un spec de casos de uso (formato de cu_detalle.vbs): ninguna línea (ASOC, DEP o GEN, de centro a centro) debe
atravesar una elipse o un actor que no sea uno de sus extremos, ninguna elipse debe tocar a otra y ninguna relación
puede nombrar un actor o caso que todavía no se haya definido (cu_detalle.vbs los crea en el orden del spec).
uso: verificar_cruces_cu.py <spec.txt> [...]      (sale con código 1 si hay problemas)"""
ALTO=5600; ACTOR=(5000,4600)

def leer(ruta):
    fig={}; lineas=[]; orden=[]
    for l in open(ruta,encoding='utf8'):
        p=l.rstrip('\n').split('|')
        if p[0]=='CU': fig[p[2]]=('cu',int(p[3]),int(p[4]),int(p[5])/2,ALTO/2)
        elif p[0]=='ACTOR': fig[p[2]]=('actor',int(p[3]),int(p[4]),ACTOR[0]/2,ACTOR[1]/2)
        elif p[0] in ('ASOC','DEP','GEN'):
            lineas.append((p[1],p[2]))
            orden+=[f'{p[0]} {p[1]}->{p[2]} usa {k} antes de definirlo' for k in p[1:3] if k not in fig]
    return fig,lineas,orden

def dentro(f,x,y,margen=1.0):
    t,cx,cy,a,b=f
    if t=='cu': return ((x-cx)/(a*margen))**2+((y-cy)/(b*margen))**2<1
    return abs(x-cx)<a*margen and abs(y-cy)<b*margen

def problemas(ruta):
    fig,lineas,out=leer(ruta)
    for o,d in lineas:
        _,x1,y1,*_=fig[o]; _,x2,y2,*_=fig[d]
        for k,f in fig.items():
            if k in (o,d): continue
            if any(dentro(f,x1+(x2-x1)*i/400,y1+(y2-y1)*i/400,1.08) for i in range(401)):
                out.append(f'{o}->{d} cruza {k}')
    nombres=list(fig)
    for i,a in enumerate(nombres):
        for b in nombres[i+1:]:
            fa,fb=fig[a],fig[b]
            if fa[0]=='cu' and fb[0]=='cu' and abs(fa[2]-fb[2])<ALTO+600 and abs(fa[1]-fb[1])<fa[3]+fb[3]+800:
                out.append(f'{a} toca {b}')
    return out

if __name__=='__main__':
    malo=False
    for r in sys.argv[1:]:
        p=problemas(r)
        print(('OK  ' if not p else 'MAL ')+r.split('/')[-1].split('\\')[-1]+('' if not p else ': '+'; '.join(p)))
        malo|=bool(p)
    sys.exit(1 if malo else 0)
