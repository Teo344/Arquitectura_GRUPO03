# Herramientas para los manuales técnicos (GR03)

```
HERRAMIENTAS_MANUALES/
├── instrucciones/
│   ├── GUIA_GENERACION_MANUALES.md   ← estructura, decisiones, proceso y problemas resueltos
│   └── GUIA_DIAGRAMAS_POWERDESIGNER.md ← diagramas UML de EurekaBank en PowerDesigner
└── herramientas/
    ├── powerdesigner/                ← scripts de los diagramas (ver la guía de diagramas)
    ├── build.py                      ← genera el .docx
    ├── docxlib.py                    ← formato de la plantilla (títulos, pasos, figuras, tablas)
    ├── actualizar_indices.ps1        ← actualiza los índices con Word y exporta a PDF
    ├── requirements.txt
    ├── plantilla/plantilla_gr03.docx ← portada e índices del grupo
    └── contenidos/                   ← un módulo por manual
        ├── contenido_soap_dotnet_sinbdd.py
        └── contenido_restful_dotnet_sinbdd.py
```

## Pedir un manual nuevo

> Lee `HERRAMIENTAS_MANUALES/instrucciones/GUIA_GENERACION_MANUALES.md` y construye el manual del proyecto `TI.X.Y. …`

## Continuar con los diagramas UML (PowerDesigner)

> Lee `HERRAMIENTAS_MANUALES/instrucciones/GUIA_DIAGRAMAS_POWERDESIGNER.md` y continúa con los diagramas de actividad, secuencia, clases y arquitectura empezando por TG.1.1.

## Uso rápido

```
cd HERRAMIENTAS_MANUALES/herramientas
python -m pip install -r requirements.txt
python build.py ../salida/MANUAL.docx contenido_restful_dotnet_sinbdd
pwsh -File actualizar_indices.ps1 -Docx ../salida/MANUAL.docx -Pdf
```

La carpeta `salida/` está ignorada por git. Cuando el manual esté revisado, se copia a `05. DOCUMENTACION` del proyecto.
