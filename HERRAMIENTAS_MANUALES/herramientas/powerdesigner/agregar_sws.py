import sys,re
"""Agrega (o reemplaza) un proyecto en el espacio de trabajo .sws sin quitar los existentes.
uso: agregar_sws.py <archivo.sws> <nombre_proyecto> <guid_prj> <url_relativa_prj>"""
sws,nombre,guid,url=sys.argv[1:5]
x=open(sws,encoding='utf8').read()
x=re.sub(r'\t\t<Model [^\n]*Name="'+re.escape(nombre)+r'"[^\n]*/>\r?\n','',x)
x=x.replace(' Selected="Yes"','')
linea=f'\t\t<Model Expanded="Yes" ID="{{{guid}}}" Name="{nombre}" Type="{{FF4B6538-9856-42CF-B9B9-304ED370CEAA}}" URL="{url}"/>\r\n'
x=re.sub(r'(\t</Local>)',lambda m: linea+m.group(1),x,count=1)
# los proyectos quedan ordenados por nombre (01_, 02_, ...)
ini=x.index('\t\t<Model'); fin=x.index('\t</Local>')
modelos=sorted(re.findall(r'\t\t<Model [^\n]*\n',x[ini:fin]),key=lambda l: re.search(r'Name="([^"]+)"',l).group(1))
x=x[:ini]+''.join(modelos)+x[fin:]
open(sws,'w',encoding='utf8',newline='').write(x)
print(''.join(modelos))
