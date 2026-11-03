import sys,re,uuid
"""Ajusta un diagrama de casos de uso generado por cu_detalle.vbs desde la plantilla de TI.1.1:
- pone el título del límite del sistema (TextSymbol.Text no se puede escribir por COM);
- agrega los límites de sistema adicionales de las líneas CAJA|izq|arriba|der|abajo|titulo del spec (por ejemplo, el del
  Core BanQuito cuando un caso de la Comercializadora incluye un caso del Core), clonando el rectángulo y el título de la
  plantilla para conservar su estilo;
- asigna GUID nuevos a lo heredado de la plantilla.
uso: finalizar_cu.py <oom> <plantilla_oom> <spec.txt (UTF-8)> <titulo>"""
nuevo,plantilla,spec,titulo=sys.argv[1:5]
x=open(nuevo,encoding='utf8').read(); v=open(plantilla,encoding='utf8').read()
assert x.count('<a:Text>PROCESO DE CONVERSIÓN</a:Text>')==1
x=x.replace('<a:Text>PROCESO DE CONVERSIÓN</a:Text>','<a:Text>'+titulo+'</a:Text>')

cajas=[l.rstrip('\n').split('|') for l in open(spec,encoding='utf8') if l.startswith('CAJA|')]
if cajas:
    sig=max(int(i) for i in re.findall(r'Id="o(\d+)"',x))+1
    rect=re.search(r'<o:RectangleSymbol Id="[^"]+">.*?</o:RectangleSymbol>',x,re.S).group(0)
    texto=re.search(r'<o:TextSymbol Id="[^"]+">.*?</o:TextSymbol>',x,re.S).group(0)
    nuevos=''
    for _,l,t,r,b,tit in cajas:
        l,t,r,b=map(int,(l,t,r,b))
        nuevos+=re.sub(r'Id="o\d+"',f'Id="o{sig}"',re.sub(r'<a:Rect>[^<]*',f'<a:Rect>(({l},{t}), ({r},{b}))',rect,count=1),count=1)+'\n'
        tx=re.sub(r'<a:Rect>[^<]*',f'<a:Rect>(({l+2000},{t-4500}), ({r-2000},{t-500}))',texto,count=1)
        tx=re.sub(r'<a:Text>[^<]*</a:Text>',lambda m:f'<a:Text>{tit}</a:Text>',tx,count=1)
        nuevos+=re.sub(r'Id="o\d+"',f'Id="o{sig+1}"',tx,count=1)+'\n'
        sig+=2
    i=x.index('<c:Symbols>',x.index('<o:UseCaseDiagram Id='))+len('<c:Symbols>\n')
    x=x[:i]+nuevos+x[i:]

pat=r'[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}'
viejos=set(re.findall(pat,v)); n=0
for g in set(re.findall(pat,x)) & viejos:
    if re.search(r'(<a:ObjectID>|ID="\{)'+g,x):
        x=x.replace(g,str(uuid.uuid4()).upper()); n+=1
open(nuevo,'w',encoding='utf8').write(x)
m=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',x)
print('cajas',len(cajas),'guid reemplazados',n,'ModelID',m.group(1))
