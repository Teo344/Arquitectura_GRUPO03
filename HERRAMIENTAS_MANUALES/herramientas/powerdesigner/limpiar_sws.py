import sys,re
"""Quita de los .sws los modelos MER sueltos que PowerDesigner agrega al espacio de trabajo activo cuando se cierra
(los modelos ya están dentro del proyecto MER).
uso: limpiar_sws.py <archivo.sws> [<archivo.sws> ...]"""
for sws in sys.argv[1:]:
    x=open(sws,encoding='utf8',newline='').read()
    y=re.sub(r'\t\t<Model [^\n]*Name="MER (Conceptual|Lógico|Físico) [^\n]*\n','',x)
    if y!=x:
        open(sws,'w',encoding='utf8',newline='').write(y)
        print(sws, x.count('\n')-y.count('\n'), 'modelos sueltos quitados')
