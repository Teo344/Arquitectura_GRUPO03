# -*- coding: utf-8 -*-
"""Genera specs/spec_<n>.txt (UTF-16) para cu_detalle.vbs: el caso crítico ECUD<n> en el centro y sus casos
incluidos/extendidos (según el ECUD) dispuestos en arco, para que ninguna flecha cruce otra elipse.
uso: python generar_specs.py"""
import math, os

NOMBRES = {1: 'Iniciar sesión', 2: 'Cerrar sesión', 3: 'Consultar cuenta', 4: 'Consultar movimientos',
           5: 'Registrar depósito', 6: 'Registrar retiro', 7: 'Registrar transferencia',
           8: 'Validar token de sesión', 9: 'Validar datos de la operación', 10: 'Registrar movimiento transaccional',
           11: 'Gestionar credenciales incorrectas', 12: 'Gestionar sesión inválida o expirada',
           13: 'Gestionar cuenta inexistente o inactiva', 14: 'Gestionar fondos insuficientes',
           15: 'Gestionar transferencia inválida', 16: 'Gestionar falla de la base de datos'}
# caso crítico: (incluidos, extendidos)
RELACIONES = {1: ([], [11, 16]), 2: ([], [12, 16]), 3: ([8, 9], [12, 13, 16]), 4: ([8, 9], [12, 13, 16]),
              5: ([8, 9, 10], [12, 13, 16]), 6: ([8, 9, 10], [12, 13, 14, 16]),
              7: ([8, 9, 10], [12, 13, 14, 15, 16])}
RADIO = 44000  # unidades de PowerDesigner

carpeta = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs')
os.makedirs(carpeta, exist_ok=True)
for n, (incluidos, extendidos) in RELACIONES.items():
    rel = [(k, 'include') for k in incluidos] + [(k, 'extend') for k in extendidos]
    k = len(rel)
    apertura = min(65, 12.5 * (k - 1)) if k > 1 else 0
    angulos = [apertura - 2 * apertura * i / (k - 1) for i in range(k)] if k > 1 else [0]
    lineas = ['ACTOR|Empleado (cajero)|Empleado|-34000|0',
              f'CU|ECUD{n}: {NOMBRES[n]}|ECUD{n}|0|0|24000',
              f'ASOC|Empleado|ECUD{n}']
    for (c, tipo), a in zip(rel, angulos):
        x = int(RADIO * math.cos(math.radians(a))); y = int(RADIO * math.sin(math.radians(a)))
        lineas.append(f'CU|ECUD{c}: {NOMBRES[c]}|ECUD{c}|{x}|{y}|27000')
        # include: del caso base al incluido; extend: del caso de error al caso base
        lineas.append(f'DEP|ECUD{n}|ECUD{c}|include' if tipo == 'include' else f'DEP|ECUD{c}|ECUD{n}|extend')
    alto = max(int(RADIO * math.sin(math.radians(apertura))), 6000)
    arriba, abajo, derecha = alto + 11000, -(alto + 7000), RADIO + 16000
    lineas.append(f'RECT|-16000|{arriba}|{derecha}|{abajo}')
    lineas.append(f'TITULO|{(derecha - 16000) // 2 - 24000}|{arriba - 500}|{(derecha - 16000) // 2 + 20000}|{arriba - 4500}')
    with open(os.path.join(carpeta, f'spec_{n}.txt'), 'w', encoding='utf-16') as f:
        f.write('\n'.join(lineas) + '\n')
print('specs generados en', carpeta)
