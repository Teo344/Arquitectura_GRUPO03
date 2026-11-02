import sys,re,uuid
"""Ajusta un diagrama de actividad generado desde la plantilla de TI.1.1: lleva los flujos al frente (si quedan antes
de las calles, estas los tapan) y asigna GUID nuevos a los objetos heredados.
uso: finalizar_da.py <oom> <plantilla_oom>"""
nuevo,plantilla=sys.argv[1:3]
x=open(nuevo,encoding='utf8').read(); v=open(plantilla,encoding='utf8').read()
a=x.index('<c:Symbols>'); g=x.index('<o:SwimlaneGroupSymbol',a)
flujos=re.findall(r'\s*<o:FlowSymbol Id="[^"]+">.*?</o:FlowSymbol>',x[a:g],re.S)
assert flujos, 'no hay flujos antes de las calles'
cuerpo=x[a:g]
for fl in flujos: cuerpo=cuerpo.replace(fl,'',1)
x=x[:a]+cuerpo+x[g:]
fin=x.index('</c:Symbols>',x.index('<o:SwimlaneGroupSymbol',a))
# el primer </c:Symbols> después del grupo puede ser de las calles: se busca el cierre del nivel superior
prof=0; i=a+len('<c:Symbols>')
for t in re.finditer(r'<(/?)c:Symbols>',x[i:]):
    if t.group(1):
        if prof==0: fin=i+t.start(); break
        prof-=1
    else: prof+=1
x=x[:fin]+''.join(flujos)+'\n'+x[fin:]
pat=r'[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}'
viejos=set(re.findall(pat,v)); n=0
for gu in set(re.findall(pat,x)) & viejos:
    if re.search(r'(<a:ObjectID>|ID="\{)'+gu,x):
        x=x.replace(gu,str(uuid.uuid4()).upper()); n+=1
open(nuevo,'w',encoding='utf8').write(x)
m=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',x)
print('flujos',len(flujos),'guid reemplazados',n,'ModelID',m.group(1))
