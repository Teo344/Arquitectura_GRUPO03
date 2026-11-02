import sys,re,uuid
"""Ajusta un diagrama de clases generado por dc_detalle.vbs desde la plantilla de TI.1.1:
- dibuja un recuadro con el nombre de cada paquete (los rectángulos y textos libres no se pueden crear por COM);
- asigna GUID nuevos a los objetos heredados de la plantilla.
uso: finalizar_dc.py <oom> <plantilla_oom> <paquetes.txt>"""
nuevo,plantilla,paquetes=sys.argv[1:4]
x=open(nuevo,encoding='utf8').read(); v=open(plantilla,encoding='utf8').read()
sig=max(int(i) for i in re.findall(r'Id="o(\d+)"',x))+1
fecha=re.search(r'<a:CreationDate>(\d+)',x).group(1)

simbolos=''
for linea in open(paquetes,encoding='utf-16').read().split('\n'):
    p=linea.strip().split('|')
    if p[0]!='PAQ': continue
    titulo=p[1]; l,t,r,b=map(int,p[2:6])
    simbolos+=(f'<o:RectangleSymbol Id="o{sig}">\n<a:CreationDate>{fecha}</a:CreationDate>\n'
               f'<a:ModificationDate>{fecha}</a:ModificationDate>\n<a:Rect>(({l},{t}), ({r},{b}))</a:Rect>\n'
               '<a:TextStyle>4130</a:TextStyle>\n<a:AutoAdjustToText>0</a:AutoAdjustToText>\n'
               '<a:LineColor>16711680</a:LineColor>\n<a:FillColor>16777215</a:FillColor>\n'
               '<a:ShadowColor>12632256</a:ShadowColor>\n<a:FontName>Arial,8,N</a:FontName>\n'
               '<a:ManuallyResized>1</a:ManuallyResized>\n</o:RectangleSymbol>\n')
    sig+=1
    simbolos+=(f'<o:TextSymbol Id="o{sig}">\n<a:Text>{titulo}</a:Text>\n<a:CreationDate>{fecha}</a:CreationDate>\n'
               f'<a:ModificationDate>{fecha}</a:ModificationDate>\n<a:Rect>(({l+800},{t-2600}), ({r-800},{t-300}))</a:Rect>\n'
               '<a:TextStyle>4130</a:TextStyle>\n<a:AutoAdjustToText>0</a:AutoAdjustToText>\n<a:LineColor>0</a:LineColor>\n'
               '<a:DashStyle>7</a:DashStyle>\n<a:FillColor>0</a:FillColor>\n<a:ShadowColor>12632256</a:ShadowColor>\n'
               '<a:FontName>Arial,10,B</a:FontName>\n<a:ManuallyResized>1</a:ManuallyResized>\n</o:TextSymbol>\n')
    sig+=1
# al principio de los símbolos del diagrama, para que queden detrás de las clases
i=x.index('<c:Symbols>',x.index('<o:ClassDiagram Id='))+len('<c:Symbols>\n')
x=x[:i]+simbolos+x[i:]

pat=r'[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}'
viejos=set(re.findall(pat,v)); n=0
for gu in set(re.findall(pat,x)) & viejos:
    if re.search(r'(<a:ObjectID>|ID="\{)'+gu,x):
        x=x.replace(gu,str(uuid.uuid4()).upper()); n+=1
open(nuevo,'w',encoding='utf8').write(x)
m=re.search(r'<o:Model Id="o2">\s*<a:ObjectID>([^<]+)',x)
print('paquetes',simbolos.count('<o:RectangleSymbol'),'guid reemplazados',n,'ModelID',m.group(1))
