import sys,re,uuid
"""Ajusta un .oom generado desde la plantilla de TI.1.1: título del límite y GUID nuevos para los objetos heredados.
uso: finalizar_oom.py <oom> <plantilla_oom> <titulo>"""
nuevo,plantilla,titulo=sys.argv[1:4]
x=open(nuevo,encoding='utf8').read(); v=open(plantilla,encoding='utf8').read()
assert x.count('<a:Text>PROCESO DE CONVERSIÓN</a:Text>')==1
x=x.replace('<a:Text>PROCESO DE CONVERSIÓN</a:Text>','<a:Text>'+titulo+'</a:Text>')
pat=r'[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}'
viejos=set(re.findall(pat,v)); n=0
for g in set(re.findall(pat,x)) & viejos:
    if re.search(r'(<a:ObjectID>|ID="\{)'+g,x):
        x=x.replace(g,str(uuid.uuid4()).upper()); n+=1
open(nuevo,'w',encoding='utf8').write(x)
m=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',x)
print('guid reemplazados',n,'ModelID',m.group(1))
