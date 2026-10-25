# Uso: python build.py <salida.docx> <modulo de contenidos/>
#   p. ej. python build.py C:/temp/MANUAL.docx contenido_restful_dotnet_sinbdd
import importlib, os, re, shutil, sys, tempfile, zipfile
from lxml import etree

sys.stdout.reconfigure(encoding='utf-8')
BASE = os.path.dirname(os.path.abspath(__file__))
# Portada e índices del manual GR03 (SOAP .NET sin BDD) sin el cuerpo
PLANTILLA = os.path.join(BASE, 'plantilla', 'plantilla_gr03.docx')
sys.path.insert(0, os.path.join(BASE, 'contenidos'))
if len(sys.argv) < 3:
    sys.exit('Uso: python build.py <salida.docx> <modulo de contenidos/>')
SALIDA = os.path.abspath(sys.argv[1])
# Módulo con construir() y, opcionalmente, PORTADA = [(texto plantilla, texto nuevo), ...]
modulo = importlib.import_module(sys.argv[2].removesuffix('.py'))
construir = modulo.construir

TRABAJO = tempfile.mkdtemp(prefix='manual_')
with zipfile.ZipFile(PLANTILLA) as z:
    z.extractall(TRABAJO)

W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
q = lambda n: '{%s}%s' % (W, n)

ruta_doc = os.path.join(TRABAJO, 'word', 'document.xml')
arbol = etree.parse(ruta_doc)
raiz = arbol.getroot()
body = raiz.find(q('body'))
hijos = list(body)

# Portada + índices = elementos antes de "INTRODUCCIÓN"; el sectPr final se conserva
inicio = next(i for i, k in enumerate(hijos) if ''.join(k.itertext()).strip() == 'INTRODUCCIÓN')
for k in hijos[inicio:-1]:
    body.remove(k)
# Título de la portada (cuadros de texto) y de la página de índices
for viejo, nuevo in getattr(modulo, 'PORTADA', []):
    for k in hijos[:inicio]:
        for nodo in k.iter(q('t')):
            if nodo.text and viejo in nodo.text:
                nodo.text = nodo.text.replace(viejo, nuevo)
# Párrafos vacíos de relleno entre el último índice y el contenido (el salto lo da pageBreakBefore)
ultimo_sdt = max(i for i, k in enumerate(hijos[:inicio]) if k.tag == q('sdt'))
for k in hijos[ultimo_sdt + 1:inicio]:
    if k.tag == q('p') and not ''.join(k.itertext()).strip() and k.find('.//' + q('drawing')) is None:
        body.remove(k)

# Los índices guardan entradas del manual Java: se dejan vacíos para que Word los regenere
AVISO = {'Heading 1': 'Índice de contenido: clic derecho > Actualizar campos.',
         'Heading 4': 'Índice de ilustraciones: clic derecho > Actualizar campos.',
         'Heading 5': 'Índice de tablas: clic derecho > Actualizar campos.'}
for sdt in body.iter(q('sdt')):
    contenido = sdt.find(q('sdtContent'))
    instr = ''.join(x.text or '' for x in contenido.iter(q('instrText')))
    if 'TOC' not in instr:
        continue
    aviso = next(v for k, v in AVISO.items() if k in instr)
    # Campos independientes del separador de listas regional: contenido por niveles de esquema,
    # ilustraciones (Título 4) y tablas (Título 5) por nivel de esquema: \t depende del idioma
    # y del separador de listas de Windows, \o no
    instr = {'Heading 1': r' TOC \o "1-3" \h \z \u ',
             'Heading 4': r' TOC \o "4-4" \h \z \u ',
             'Heading 5': r' TOC \o "5-5" \h \z \u '}[next(k for k in AVISO if k in instr)]
    primero = contenido.find(q('p'))
    ppr = primero.find(q('pPr'))
    ppr_xml = etree.tostring(ppr, encoding='unicode') if ppr is not None else ''
    for p in list(contenido):
        contenido.remove(p)
    ns = f'xmlns:w="{W}"'
    frag = (f'<w:p {ns}>{ppr_xml}<w:r><w:fldChar w:fldCharType="begin" w:dirty="true"/></w:r>'
            f'<w:r><w:instrText xml:space="preserve">{instr.replace("&", "&amp;").replace("<", "&lt;")}</w:instrText></w:r>'
            f'<w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>{aviso}</w:t></w:r>'
            f'<w:r><w:fldChar w:fldCharType="end"/></w:r></w:p>')
    contenido.append(etree.fromstring(frag))

# Insertar el contenido nuevo antes del sectPr final
doc = construir()
envoltorio = etree.fromstring(f'<w:body xmlns:w="{W}">{doc.xml()}</w:body>')
sect = body[-1]
for k in list(envoltorio):
    sect.addprevious(k)
arbol.write(ruta_doc, xml_declaration=True, encoding='UTF-8', standalone=True)

# Quitar relaciones e imágenes del manual anterior que ya no se usan
texto_doc = open(ruta_doc, encoding='utf-8').read()
usados = set(re.findall(r'r:(?:embed|id|link)="(rId\d+)"', texto_doc))
ruta_rels = os.path.join(TRABAJO, 'word', '_rels', 'document.xml.rels')
rels = etree.parse(ruta_rels)
for rel in list(rels.getroot()):
    tipo = rel.get('Type').rsplit('/', 1)[-1]
    if tipo in ('image', 'hyperlink') and rel.get('Id') not in usados:
        if tipo == 'image':
            os.remove(os.path.join(TRABAJO, 'word', rel.get('Target')))
        rels.getroot().remove(rel)
rels.write(ruta_rels, xml_declaration=True, encoding='UTF-8', standalone=True)

# Entradas de los índices de ilustraciones y tablas sin la sangría de los niveles 4 y 5
ruta_estilos = os.path.join(TRABAJO, 'word', 'styles.xml')
s = open(ruta_estilos, encoding='utf-8').read()
estilos_toc = ''.join(
    f'<w:style w:type="paragraph" w:styleId="TDC{n}"><w:name w:val="toc {n}"/><w:basedOn w:val="Normal"/>'
    f'<w:next w:val="Normal"/><w:autoRedefine/><w:uiPriority w:val="39"/><w:unhideWhenUsed/>'
    f'<w:pPr><w:spacing w:after="100"/><w:ind w:left="0"/></w:pPr></w:style>' for n in (4, 5))
s = s.replace('</w:styles>', estilos_toc + '</w:styles>')
open(ruta_estilos, 'w', encoding='utf-8').write(s)

# Pedir a Word que actualice los índices al abrir
ruta_set = os.path.join(TRABAJO, 'word', 'settings.xml')
s = open(ruta_set, encoding='utf-8').read()
if 'w:updateFields' not in s:
    # El esquema exige que updateFields vaya justo antes de estos elementos
    m = re.search(r'<w:(hdrShapeDefaults|footnotePr|endnotePr|compat|docVars|rsids)\b', s)
    s = s[:m.start()] + '<w:updateFields w:val="true"/>' + s[m.start():]
open(ruta_set, 'w', encoding='utf-8').write(s)

if os.path.exists(SALIDA):
    os.remove(SALIDA)
with zipfile.ZipFile(SALIDA, 'w', zipfile.ZIP_DEFLATED) as z:
    # [Content_Types].xml primero
    z.write(os.path.join(TRABAJO, '[Content_Types].xml'), '[Content_Types].xml')
    for carpeta, _, archivos in os.walk(TRABAJO):
        for a in archivos:
            completo = os.path.join(carpeta, a)
            rel = os.path.relpath(completo, TRABAJO).replace('\\', '/')
            if rel != '[Content_Types].xml':
                z.write(completo, rel)
shutil.rmtree(TRABAJO, ignore_errors=True)
print('OK', SALIDA, 'figuras:', doc.figura, 'tablas:', doc.tabla)
