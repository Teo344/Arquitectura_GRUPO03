# Helpers que generan XML WordprocessingML replicando el formato de la plantilla del manual GR03.
from xml.sax.saxutils import escape

RPR_BODY = '<w:rFonts w:eastAsia="Calibri"/><w:b w:val="0"/><w:bCs w:val="0"/><w:sz w:val="22"/><w:szCs w:val="22"/>'
RPR_CAP = '<w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:color w:val="auto"/>'
RPR_CODE = ('<w:rFonts w:ascii="Calibri" w:eastAsia="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>'
            '<w:b w:val="0"/><w:bCs w:val="0"/><w:color w:val="4F81BD"/><w:sz w:val="22"/><w:szCs w:val="22"/>'
            '<w:lang w:val="en-US"/>')
TBL_BORDERS = ('<w:tblBorders>' + ''.join(
    f'<w:{s} w:val="single" w:sz="8" w:space="0" w:color="000000"/>'
    for s in ('top', 'left', 'bottom', 'right', 'insideH', 'insideV')) + '</w:tblBorders>')
ANCHO = 8504


def t(texto):
    return f'<w:t xml:space="preserve">{escape(texto)}</w:t>'


def runs(texto, rpr=RPR_BODY):
    """Texto con **negrita** y `código` en línea."""
    out, i = [], 0
    import re
    for m in re.finditer(r'\*\*(.+?)\*\*|`(.+?)`', texto):
        if m.start() > i:
            out.append(f'<w:r><w:rPr>{rpr}</w:rPr>{t(texto[i:m.start()])}</w:r>')
        if m.group(1) is not None:
            negrita = rpr.replace('<w:b w:val="0"/><w:bCs w:val="0"/>', '<w:b/><w:bCs/>')
            out.append(f'<w:r><w:rPr>{negrita}</w:rPr>{t(m.group(1))}</w:r>')
        else:
            out.append(f'<w:r><w:rPr>{rpr}<w:i/><w:iCs/></w:rPr>{t(m.group(2))}</w:r>')
        i = m.end()
    if i < len(texto):
        out.append(f'<w:r><w:rPr>{rpr}</w:rPr>{t(texto[i:])}</w:r>')
    return ''.join(out)


class Doc:
    def __init__(self):
        self.partes = []
        self.figura = 0
        self.tabla = 0
        self.paso = 0
        self.h1 = 0

    def add(self, xml):
        self.partes.append(xml)

    def h1_(self, texto):
        self.h1 += 1
        self.add('<w:p><w:pPr><w:pStyle w:val="Ttulo1"/><w:pageBreakBefore/><w:numPr><w:ilvl w:val="0"/><w:numId w:val="5"/></w:numPr>'
                 '<w:spacing w:before="120" w:after="120" w:line="240" w:lineRule="auto"/><w:jc w:val="left"/>'
                 '<w:rPr><w:rFonts w:eastAsia="Calibri"/><w:sz w:val="40"/><w:szCs w:val="40"/></w:rPr></w:pPr>'
                 f'<w:r><w:rPr><w:rFonts w:eastAsia="Calibri"/><w:b/><w:bCs/><w:sz w:val="40"/><w:szCs w:val="40"/></w:rPr>{t(texto)}</w:r></w:p>')

    def h2(self, texto, reinicia_pasos=True):
        if reinicia_pasos:
            self.paso = 0
        self.add('<w:p><w:pPr><w:pStyle w:val="Ttulo2"/><w:numPr><w:ilvl w:val="1"/><w:numId w:val="5"/></w:numPr>'
                 '<w:spacing w:before="120" w:after="120" w:line="240" w:lineRule="auto"/><w:jc w:val="both"/>'
                 '<w:rPr><w:rFonts w:eastAsia="Calibri"/><w:color w:val="auto"/><w:sz w:val="36"/><w:szCs w:val="36"/></w:rPr></w:pPr>'
                 f'<w:r><w:rPr><w:rFonts w:eastAsia="Calibri"/><w:color w:val="auto"/><w:sz w:val="36"/><w:szCs w:val="36"/></w:rPr>{t(texto)}</w:r></w:p>')

    def paso_(self, titulo):
        self.paso += 1
        self.add('<w:p><w:pPr><w:pStyle w:val="Ttulo3"/><w:numPr><w:ilvl w:val="2"/><w:numId w:val="5"/></w:numPr>'
                 '<w:rPr><w:rFonts w:eastAsia="Calibri"/></w:rPr></w:pPr>'
                 f'<w:r><w:rPr><w:rFonts w:eastAsia="Calibri"/></w:rPr>{t(f"Paso {self.paso}: {titulo}")}</w:r></w:p>')

    def p(self, texto, centro=False):
        jc = 'center' if centro else 'both'
        self.add(f'<w:p><w:pPr><w:spacing w:after="160" w:line="259" w:lineRule="auto"/><w:jc w:val="{jc}"/>'
                 f'<w:rPr>{RPR_BODY}</w:rPr></w:pPr>{runs(texto)}</w:p>')

    def vineta(self, texto, num=8, jc='both'):
        self.add(f'<w:p><w:pPr><w:pStyle w:val="Prrafodelista"/><w:numPr><w:ilvl w:val="0"/><w:numId w:val="{num}"/></w:numPr>'
                 f'<w:spacing w:after="80" w:line="259" w:lineRule="auto"/><w:jc w:val="{jc}"/><w:rPr>{RPR_BODY}</w:rPr></w:pPr>{runs(texto)}</w:p>')

    def vacio(self):
        self.add(f'<w:p><w:pPr><w:rPr>{RPR_BODY}</w:rPr></w:pPr></w:p>')

    def figura_(self, titulo, captura):
        """Leyenda 'Figura N' + recuadro marcador donde se pegará la captura."""
        self.figura += 1
        self.add(f'<w:p><w:pPr><w:pStyle w:val="Ttulo4"/><w:rPr>{RPR_CAP}</w:rPr></w:pPr>'
                 f'<w:r><w:rPr>{RPR_CAP}</w:rPr>{t(f"Figura {self.figura}")}</w:r>'
                 f'<w:r><w:rPr>{RPR_CAP}</w:rPr><w:br/>{t(titulo)}</w:r></w:p>')
        borde = ''.join(f'<w:{s} w:val="dashed" w:sz="6" w:space="8" w:color="7F7F7F"/>' for s in ('top', 'left', 'bottom', 'right'))
        rpr = '<w:rFonts w:eastAsia="Calibri"/><w:b w:val="0"/><w:bCs w:val="0"/><w:i/><w:iCs/><w:color w:val="7F7F7F"/><w:sz w:val="20"/><w:szCs w:val="20"/>'
        self.add(f'<w:p><w:pPr><w:pBdr>{borde}</w:pBdr><w:shd w:val="clear" w:color="auto" w:fill="F2F2F2"/>'
                 f'<w:spacing w:before="240" w:after="360" w:line="259" w:lineRule="auto"/><w:ind w:left="567" w:right="567"/><w:jc w:val="center"/>'
                 f'<w:rPr>{rpr}</w:rPr></w:pPr>'
                 f'<w:r><w:rPr>{rpr}</w:rPr><w:br/>{t("[Insertar captura: " + captura + "]")}<w:br/></w:r></w:p>')

    def leyenda_tabla(self, titulo):
        self.tabla += 1
        self.add(f'<w:p><w:pPr><w:pStyle w:val="Ttulo5"/><w:spacing w:line="240" w:lineRule="auto"/><w:rPr>{RPR_CAP}</w:rPr></w:pPr>'
                 f'<w:r><w:rPr>{RPR_CAP}<w:i/><w:iCs/></w:rPr>{t(f"Tabla {self.tabla}")}</w:r>'
                 f'<w:r><w:rPr>{RPR_CAP}<w:i/><w:iCs/></w:rPr><w:br/></w:r>'
                 f'<w:r><w:rPr>{RPR_CAP}</w:rPr>{t(titulo)}</w:r></w:p>')

    def codigo(self, titulo, texto):
        """Tabla de una celda con el código fuente, como en la plantilla."""
        self.leyenda_tabla(titulo)
        lineas = texto.replace('\r\n', '\n').replace('\t', '    ').lstrip('﻿').rstrip('\n').split('\n')
        ps = []
        for linea in lineas:
            r = f'<w:r><w:rPr>{RPR_CODE}</w:rPr>{t(linea)}</w:r>' if linea else ''
            ps.append(f'<w:p><w:pPr><w:widowControl w:val="0"/><w:spacing w:line="240" w:lineRule="auto"/>'
                      f'<w:rPr>{RPR_CODE}</w:rPr></w:pPr>{r}</w:p>')
        self.add('<w:tbl><w:tblPr><w:tblStyle w:val="a"/>'
                 f'<w:tblW w:w="{ANCHO}" w:type="dxa"/><w:tblInd w:w="0" w:type="dxa"/>{TBL_BORDERS}'
                 '<w:tblLayout w:type="fixed"/><w:tblLook w:val="0600" w:firstRow="0" w:lastRow="0" w:firstColumn="0" w:lastColumn="0" w:noHBand="1" w:noVBand="1"/></w:tblPr>'
                 f'<w:tblGrid><w:gridCol w:w="{ANCHO}"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w="{ANCHO}" w:type="dxa"/>'
                 '<w:tcMar><w:top w:w="100" w:type="dxa"/><w:left w:w="100" w:type="dxa"/><w:bottom w:w="100" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tcMar></w:tcPr>'
                 + ''.join(ps) + '</w:tc></w:tr></w:tbl>')
        self.vacio()

    def datos(self, titulo, cabecera, filas, anchos):
        """Tabla de datos con cabecera sombreada."""
        self.leyenda_tabla(titulo)
        assert sum(anchos) == ANCHO

        def celda(texto, w, cab):
            sh = '<w:shd w:val="clear" w:color="auto" w:fill="D9E2F3"/>' if cab else ''
            rpr = RPR_BODY.replace('<w:sz w:val="22"/><w:szCs w:val="22"/>', '<w:sz w:val="20"/><w:szCs w:val="20"/>')
            if cab:
                rpr = rpr.replace('<w:b w:val="0"/><w:bCs w:val="0"/>', '<w:b/><w:bCs/>')
            return (f'<w:tc><w:tcPr><w:tcW w:w="{w}" w:type="dxa"/>{sh}</w:tcPr>'
                    f'<w:p><w:pPr><w:spacing w:before="40" w:after="40" w:line="240" w:lineRule="auto"/><w:rPr>{rpr}</w:rPr></w:pPr>{runs(texto, rpr)}</w:p></w:tc>')
        filas_xml = ''.join('<w:tr>' + ''.join(celda(c, w, i == 0) for c, w in zip(f, anchos)) + '</w:tr>'
                            for i, f in enumerate([cabecera] + filas))
        self.add('<w:tbl><w:tblPr>'
                 f'<w:tblW w:w="{ANCHO}" w:type="dxa"/>{TBL_BORDERS}<w:tblLayout w:type="fixed"/>'
                 '<w:tblCellMar><w:left w:w="100" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tblCellMar></w:tblPr>'
                 '<w:tblGrid>' + ''.join(f'<w:gridCol w:w="{w}"/>' for w in anchos) + '</w:tblGrid>'
                 + filas_xml + '</w:tbl>')
        self.vacio()

    def xml(self):
        return ''.join(self.partes)
