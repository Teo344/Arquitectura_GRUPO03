import os,math
"""Genera specs/casos_uso/EX11/*.txt (UTF-8) para cu_detalle.vbs: los casos de uso del EX.1.1 (BanQuito y Comercializadora
de celulares), tomados del ERS y del ECUD de EX.1.1:
- dos diagramas generales (ilustraciones 1 y 2 del ECUD): Core BanQuito (actor Comercializadora) y Comercializadora
  (actor Empleado, sus cuatro clientes y el Core BanQuito como actor secundario en RF-07, 08, 09, 10 y 12);
- un diagrama por ECUD (ilustraciones 3 a 20). Solo hay <<include>>: el ECUD no define casos extendidos.
Cuando un caso de la Comercializadora incluye un caso del Core (ECUD7, ECUD10, ECUD12), este se dibuja en un segundo
límite de sistema (CAJA). Cuando usa el Core sin un caso de uso del Core (simulación, buscar cliente, listar créditos),
el Core aparece como actor secundario.
La primera línea de cada spec es '#|<modelo>|<nombre del diagrama>|<título del límite>'.
uso: generar_specs_cu_ex.py"""

NOMBRES={1:'Iniciar sesión',2:'Cerrar sesión',3:'Consultar catálogo de teléfonos',4:'Registrar teléfono',
         5:'Editar teléfono',6:'Eliminar teléfono',7:'Verificar al comprador',8:'Simular tabla de amortización',
         9:'Vender en efectivo',10:'Vender con crédito directo',11:'Consultar facturas',
         12:'Consultar créditos de un comprador',13:'Verificar sujeto de crédito',14:'Calcular monto máximo',
         15:'Otorgar crédito',16:'Consultar tabla de amortización',17:'Validar token de sesión',18:'Validar clave de API'}
ARCHIVOS={1:'CU_Iniciar_Sesion',2:'CU_Cerrar_Sesion',3:'CU_Consultar_Catalogo',4:'CU_Registrar_Telefono',
          5:'CU_Editar_Telefono',6:'CU_Eliminar_Telefono',7:'CU_Verificar_Comprador',8:'CU_Simular_Tabla',
          9:'CU_Vender_Efectivo',10:'CU_Vender_Credito',11:'CU_Consultar_Facturas',12:'CU_Consultar_Creditos',
          13:'CU_Verificar_Sujeto_Credito',14:'CU_Calcular_Monto_Maximo',15:'CU_Otorgar_Credito',
          16:'CU_Consultar_Tabla_Amortizacion',17:'CU_Validar_Token_Sesion',18:'CU_Validar_Clave_API'}
COMERCIALIZADORA='COMERCIALIZADORA DE TELÉFONOS CELULARES'; CORE='CORE BANQUITO – MÓDULO DE CRÉDITO'
EMPLEADO='ACTOR|Empleado (vendedor)|Empleado'
COMERC='ACTOR|Comercializadora|Comercializadora'
CORE_ACTOR='ACTOR|Core BanQuito|CoreBanQuito'
# relaciones del ECUD (campo "Casos de uso incluidos") y del ERS (ACT-007: el Core participa en RF-07, 08, 09, 10 y 12)
INCLUYE={n:[17] for n in range(2,13)}; INCLUYE.update({n:[18] for n in range(13,17)})
INCLUYE_CORE={7:[13,14],10:[13,14,15],12:[16]}
CORE_SECUNDARIO={8,9,12}      # usan operaciones de apoyo del Core que no son casos de uso del ECUD
def cu(n,x,y,ancho): return f'CU|ECUD{n}: {NOMBRES[n]}|ECUD{n}|{x}|{y}|{ancho}'
def titulo_rect(l,t,r): return f'TITULO|{l+2000}|{t-500}|{r-2000}|{t-4500}'

specs={}
# ---------- general del Core BanQuito (ilustración 1)
L=['#|CU_General_Core_BanQuito|Casos de uso del Core BanQuito|'+CORE,
   COMERC+'|-40000|0|sistema',cu(18,44000,0,26000)]   # el incluido se crea antes que las dependencias
for n,y in zip((13,14,15,16),(21000,7000,-7000,-21000)):
    L+=[cu(n,0,y,30000),f'ASOC|Comercializadora|ECUD{n}',f'DEP|ECUD{n}|ECUD18|include']
L+=['RECT|-20000|33000|62000|-29000',titulo_rect(-20000,33000,62000)]
specs['CU_General_Core_BanQuito']=L
# ---------- general de la Comercializadora (ilustración 2): los 12 casos en arco alrededor del Empleado, para que
# ninguna asociación cruce otra elipse (radio y apertura validados con verificar_cruces_cu.py)
L=['#|CU_General_Comercializadora|Casos de uso de la Comercializadora|'+COMERCIALIZADORA,
   EMPLEADO+'|0|0']
for cod,nom,y in (('ClienteConsola','Cliente Consola',33000),('ClienteEscritorio','Cliente Escritorio',11000),
                  ('ClienteWeb','Cliente Web',-11000),('ClienteMovil','Cliente Móvil Android',-33000)):
    L+=[f'ACTOR|{nom}|{cod}|-28000|{y}',f'GEN|{cod}|Empleado']
for i,n in enumerate(range(1,13)):
    a=math.radians(70-140*i/11)
    L+=[cu(n,int(75000*math.cos(a)),int(75000*math.sin(a)),32000),f'ASOC|Empleado|ECUD{n}']
L+=[CORE_ACTOR+'|120000|-56250|sistema']+[f'ASOC|CoreBanQuito|ECUD{n}' for n in (7,8,9,10,12)]
L+=['RECT|4000|83000|100000|-78000',titulo_rect(4000,83000,100000)]
specs['CU_General_Comercializadora']=L

# ---------- un diagrama por caso principal (ECUD1 a ECUD16)
for n in range(1,17):
    del_core=n>=13
    sistema='CORE BANQUITO' if del_core else 'COMERCIALIZADORA'
    L=[f'#|{ARCHIVOS[n]}|Caso de uso {NOMBRES[n]}|ECUD{n}: {NOMBRES[n].upper()} – {sistema}',
       (COMERC+'|-36000|0|sistema') if del_core else (EMPLEADO+'|-36000|0'),
       cu(n,0,0,28000),f'ASOC|{"Comercializadora" if del_core else "Empleado"}|ECUD{n}']
    der=20000
    for k in INCLUYE.get(n,[]):                       # inclusión dentro del mismo sistema (ECUD17 o ECUD18)
        L+=[cu(k,40000,15000,28000),f'DEP|ECUD{n}|ECUD{k}|include']; der=60000
    L+=[f'RECT|-18000|26000|{der}|-14000',titulo_rect(-18000,26000,der)]
    incl=INCLUYE_CORE.get(n,[])
    if incl:                                           # casos del Core incluidos: segundo límite de sistema
        ys={1:[6000],2:[12000,-2000],3:[16000,4000,-8000]}[len(incl)]
        for k,y in zip(incl,ys): L+=[cu(k,100000,y,32000),f'DEP|ECUD{n}|ECUD{k}|include']
        L.append(f'CAJA|78000|{ys[0]+11000}|122000|{ys[-1]-6000}|CORE BANQUITO')
    if n in CORE_SECUNDARIO:                           # el Core como actor secundario
        L+=[CORE_ACTOR+('|100000|-9000' if incl else '|80000|0')+'|sistema',f'ASOC|CoreBanQuito|ECUD{n}']
    specs[ARCHIVOS[n]]=L

# ---------- casos incluidos: ECUD17 (lo incluyen ECUD2 a ECUD12) y ECUD18 (lo incluyen ECUD13 a ECUD16)
L=[f'#|{ARCHIVOS[17]}|Caso de uso {NOMBRES[17]}|ECUD17: {NOMBRES[17].upper()} – COMERCIALIZADORA',cu(17,0,0,28000)]
for i,n in enumerate(range(2,13)):                     # arco a la izquierda (validado con verificar_cruces_cu.py)
    a=math.radians(65-130*i/10)
    L+=[cu(n,int(-70000*math.cos(a)),int(70000*math.sin(a)),32000),f'DEP|ECUD{n}|ECUD17|include']
L+=['RECT|-90000|75000|18000|-70000',titulo_rect(-90000,75000,18000)]
specs[ARCHIVOS[17]]=L
L=[f'#|{ARCHIVOS[18]}|Caso de uso {NOMBRES[18]}|ECUD18: {NOMBRES[18].upper()} – CORE BANQUITO',
   COMERC+'|-90000|0|sistema',cu(18,0,0,26000)]
for n,y in zip((13,14,15,16),(21000,7000,-7000,-21000)):
    L+=[cu(n,-46000,y,32000),f'ASOC|Comercializadora|ECUD{n}',f'DEP|ECUD{n}|ECUD18|include']
L+=['RECT|-66000|33000|18000|-29000',titulo_rect(-66000,33000,18000)]
specs[ARCHIVOS[18]]=L

carpeta=os.path.join(os.path.dirname(os.path.abspath(__file__)),'specs','casos_uso','EX11')
os.makedirs(carpeta,exist_ok=True)
for f in os.listdir(carpeta): os.remove(os.path.join(carpeta,f))
for i,(nombre,lineas) in enumerate(specs.items()):
    open(os.path.join(carpeta,f'{i:02d}_{nombre}.txt'),'w',encoding='utf8').write('\n'.join(lineas)+'\n')
print(len(specs),'specs en',carpeta)
