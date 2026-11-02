import sys,re,uuid,os
"""uso: crear_prj.py <plantilla.prj> <salida.prj> <nombre_proyecto> <modelo1.oom> [<modelo2.oom> ...]"""
plantilla,salida,nombre=sys.argv[1:4]; modelos=sys.argv[4:]
CLASES={'oom':'18112060-1A4B-11D1-83D9-444553540000','eam':'5215C79E-A9A1-4738-A627-36DA432EFE29'}  # OOM y EAM
x=open(plantilla,encoding='utf8').read()
g=lambda: str(uuid.uuid4()).upper()
pid=g()
viejo=re.search(r'<o:Project Id="o2">\s*<a:ObjectID>([^<]+)',x).group(1)
x=x.replace(viejo,pid)
x=re.sub(r'(<o:ProjectDiagram Id="o3">\s*<a:ObjectID>)[^<]+',lambda m:m.group(1)+g(),x)
x=re.sub(r'<c:Symbols>.*?</c:Symbols>\n','',x,flags=re.S)
x=re.sub(r'(<a:Name>|<a:Code>)01_CASOS_USO(</a:)',lambda m:m.group(1)+nombre+m.group(2),x)
x=re.sub(r'Name="01_CASOS_USO"','Name="'+nombre+'"',x)
docs=[]
for i,ruta in enumerate(modelos):
    o=open(ruta,encoding='utf8').read()
    mid=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',o).group(1)
    nom=os.path.splitext(os.path.basename(ruta))[0]
    ext=os.path.splitext(ruta)[1][1:].lower()
    docs.append(f"""<o:ModelDocument Id="o{100+i}">
<a:ObjectID>{g()}</a:ObjectID>
<a:Name>{nom}</a:Name>
<a:Code>{nom}</a:Code>
<a:Location>file:///%[PRJ_ROOT_DIR]%/{os.path.basename(ruta)}</a:Location>
<a:Extension>{ext}</a:Extension>
<a:ModelDocument.ModelID>{mid}</a:ModelDocument.ModelID>
<a:ModelClassID>{CLASES[ext]}</a:ModelClassID>
</o:ModelDocument>""")
x=re.sub(r'<c:ModelsAndFiles>.*?</c:ModelsAndFiles>','<c:ModelsAndFiles>\n'+'\n'.join(docs)+'\n</c:ModelsAndFiles>',x,flags=re.S)
x=re.sub(r'Objects="\d+" Symbols="\d+"',f'Objects="{3+len(docs)}" Symbols="0"',x)
open(salida,'w',encoding='utf8').write(x)
print(pid)
