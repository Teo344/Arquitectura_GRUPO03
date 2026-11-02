-- =============================================================
-- BanQuito - Grupo 03 (NRC 36884)
-- Script 02: datos de trabajo del Core
--
-- 6 clientes, 7 cuentas y 52 movimientos (la rúbrica pide al menos
-- 5, 5 y 50). Cada cliente cubre un caso de las reglas de crédito.
--
-- Todas las fechas son relativas a la fecha actual (CURDATE()), así
-- las reglas de "último mes" y "últimos 3 meses" dan el mismo
-- resultado el día que se presente:
--   * hasta 25 días atrás   -> dentro del último mes
--   * de 33 a 85 días atrás -> dentro de los últimos 3 meses
--   * 100 días o más atrás  -> fuera de la ventana de 3 meses
--
-- Resultado esperado (ver 03_consultas.sql):
--   Cliente  Caso                                Sujeto  Monto máximo
--   1 Ana    cumple todo                         SÍ      2250.00
--   2 Carlos cumple todo, promedios bajos        SÍ       450.00
--   3 Diego  hombre de 22 años (regla 3)         NO
--   4 María  sin depósitos en el último mes (2)  NO
--   5 Luis   tiene un crédito ACTIVO (regla 4)   NO
--   6 Sofía  cumple todo, con 2 cuentas          SÍ       945.00
--   Cédula 9999999999: no es cliente (regla 1)   NO
-- Monto máximo = ((promedio depósitos - promedio retiros) * 30%) * 6
-- =============================================================

USE banquito;

-- El UPDATE de saldos recorre todas las cuentas; MySQL Workbench lo
-- rechaza en modo seguro.
SET SQL_SAFE_UPDATES = 0;

-- -------------------------------------------------------------
-- Clientes (la edad se calcula desde hoy)
-- -------------------------------------------------------------
INSERT INTO CLIENTE (COD_CLIENTE, CEDULA, NOMBRE, GENERO, FECHA_NACIMIENTO) VALUES
(1, '1712345678', 'Ana Lucía Torres',        'F', DATE_SUB(CURDATE(), INTERVAL 35 YEAR)),
(2, '1723456789', 'Carlos Andrés Mena',      'M', DATE_SUB(CURDATE(), INTERVAL 40 YEAR)),
(3, '1734567890', 'Diego Fernando Ríos',     'M', DATE_SUB(CURDATE(), INTERVAL 22 YEAR)),
(4, '1745678901', 'María José Paredes',      'F', DATE_SUB(CURDATE(), INTERVAL 28 YEAR)),
(5, '1756789012', 'Luis Alberto Cevallos',   'M', DATE_SUB(CURDATE(), INTERVAL 33 YEAR)),
(6, '1767890123', 'Sofía Valentina Andrade', 'F', DATE_SUB(CURDATE(), INTERVAL 26 YEAR));

-- -------------------------------------------------------------
-- Cuentas (el saldo se calcula al final con los movimientos)
-- -------------------------------------------------------------
INSERT INTO CUENTA (NUM_CUENTA, COD_CLIENTE, SALDO) VALUES
('10000001', 1, 0),
('10000002', 2, 0),
('10000003', 3, 0),
('10000004', 4, 0),
('10000005', 5, 0),
('10000006', 6, 0),
('10000007', 6, 0);

-- -------------------------------------------------------------
-- Movimientos
-- -------------------------------------------------------------

-- Cliente 1, Ana: sujeto de crédito.
-- 3 meses: depósitos 1500, 1200, 1800, 1500 (prom. 1500); retiros 300, 400, 200, 100 (prom. 250)
-- Monto máximo = ((1500 - 250) * 0.30) * 6 = 2250.00
INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES
('10000001', 'DEP', 1500.00, DATE_SUB(CURDATE(), INTERVAL   5 DAY)),
('10000001', 'RET',  300.00, DATE_SUB(CURDATE(), INTERVAL  10 DAY)),
('10000001', 'DEP', 1200.00, DATE_SUB(CURDATE(), INTERVAL  35 DAY)),
('10000001', 'RET',  400.00, DATE_SUB(CURDATE(), INTERVAL  40 DAY)),
('10000001', 'DEP', 1800.00, DATE_SUB(CURDATE(), INTERVAL  60 DAY)),
('10000001', 'RET',  200.00, DATE_SUB(CURDATE(), INTERVAL  70 DAY)),
('10000001', 'RET',  100.00, DATE_SUB(CURDATE(), INTERVAL  80 DAY)),
('10000001', 'DEP', 1500.00, DATE_SUB(CURDATE(), INTERVAL  85 DAY)),
('10000001', 'DEP', 3000.00, DATE_SUB(CURDATE(), INTERVAL 120 DAY)),
('10000001', 'RET',  900.00, DATE_SUB(CURDATE(), INTERVAL 150 DAY));

-- Cliente 2, Carlos: sujeto de crédito con monto bajo (para probar
-- el rechazo cuando el teléfono cuesta más que el monto máximo).
-- 3 meses: depósitos 600, 500, 700 (prom. 600); retiros 400, 300 (prom. 350)
-- Monto máximo = ((600 - 350) * 0.30) * 6 = 450.00
INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES
('10000002', 'DEP',  600.00, DATE_SUB(CURDATE(), INTERVAL   8 DAY)),
('10000002', 'RET',  400.00, DATE_SUB(CURDATE(), INTERVAL  12 DAY)),
('10000002', 'DEP',  500.00, DATE_SUB(CURDATE(), INTERVAL  38 DAY)),
('10000002', 'RET',  300.00, DATE_SUB(CURDATE(), INTERVAL  45 DAY)),
('10000002', 'DEP',  700.00, DATE_SUB(CURDATE(), INTERVAL  68 DAY)),
('10000002', 'DEP', 1000.00, DATE_SUB(CURDATE(), INTERVAL 130 DAY)),
('10000002', 'RET',  200.00, DATE_SUB(CURDATE(), INTERVAL 140 DAY)),
('10000002', 'DEP',  500.00, DATE_SUB(CURDATE(), INTERVAL 200 DAY));

-- Cliente 3, Diego: hombre de 22 años; tiene depósitos recientes,
-- pero no cumple la regla 3.
INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES
('10000003', 'DEP',  800.00, DATE_SUB(CURDATE(), INTERVAL   3 DAY)),
('10000003', 'RET',  200.00, DATE_SUB(CURDATE(), INTERVAL  15 DAY)),
('10000003', 'DEP',  600.00, DATE_SUB(CURDATE(), INTERVAL  33 DAY)),
('10000003', 'RET',  100.00, DATE_SUB(CURDATE(), INTERVAL  50 DAY)),
('10000003', 'DEP',  700.00, DATE_SUB(CURDATE(), INTERVAL  63 DAY)),
('10000003', 'DEP',  500.00, DATE_SUB(CURDATE(), INTERVAL 100 DAY)),
('10000003', 'RET',   50.00, DATE_SUB(CURDATE(), INTERVAL 110 DAY)),
('10000003', 'DEP',  300.00, DATE_SUB(CURDATE(), INTERVAL 170 DAY));

-- Cliente 4, María: su último depósito fue hace 45 días; tiene un
-- retiro reciente, pero ningún depósito en el último mes (regla 2).
INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES
('10000004', 'RET',  150.00, DATE_SUB(CURDATE(), INTERVAL  10 DAY)),
('10000004', 'DEP',  900.00, DATE_SUB(CURDATE(), INTERVAL  45 DAY)),
('10000004', 'RET',  200.00, DATE_SUB(CURDATE(), INTERVAL  55 DAY)),
('10000004', 'DEP',  700.00, DATE_SUB(CURDATE(), INTERVAL  75 DAY)),
('10000004', 'DEP',  800.00, DATE_SUB(CURDATE(), INTERVAL  85 DAY)),
('10000004', 'DEP', 1000.00, DATE_SUB(CURDATE(), INTERVAL 125 DAY)),
('10000004', 'RET',  300.00, DATE_SUB(CURDATE(), INTERVAL 160 DAY)),
('10000004', 'DEP',  400.00, DATE_SUB(CURDATE(), INTERVAL 190 DAY));

-- Cliente 5, Luis: cumpliría las reglas 1 a 3, pero tiene un crédito
-- ACTIVO (regla 4), cargado al final de este script.
INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES
('10000005', 'DEP', 1000.00, DATE_SUB(CURDATE(), INTERVAL   7 DAY)),
('10000005', 'RET',  300.00, DATE_SUB(CURDATE(), INTERVAL  14 DAY)),
('10000005', 'DEP',  900.00, DATE_SUB(CURDATE(), INTERVAL  37 DAY)),
('10000005', 'RET',  250.00, DATE_SUB(CURDATE(), INTERVAL  44 DAY)),
('10000005', 'DEP', 1100.00, DATE_SUB(CURDATE(), INTERVAL  67 DAY)),
('10000005', 'DEP',  600.00, DATE_SUB(CURDATE(), INTERVAL 110 DAY)),
('10000005', 'RET',  100.00, DATE_SUB(CURDATE(), INTERVAL 135 DAY)),
('10000005', 'DEP',  700.00, DATE_SUB(CURDATE(), INTERVAL 175 DAY));

-- Cliente 6, Sofía: sujeto de crédito con 2 cuentas; los promedios
-- usan los movimientos de ambas.
-- 3 meses: depósitos 700, 900, 500, 800 (prom. 725); retiros 200, 100, 300 (prom. 200)
-- Monto máximo = ((725 - 200) * 0.30) * 6 = 945.00
INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES
('10000006', 'DEP',  700.00, DATE_SUB(CURDATE(), INTERVAL   4 DAY)),
('10000006', 'RET',  200.00, DATE_SUB(CURDATE(), INTERVAL  20 DAY)),
('10000006', 'DEP',  900.00, DATE_SUB(CURDATE(), INTERVAL  50 DAY)),
('10000006', 'RET',  100.00, DATE_SUB(CURDATE(), INTERVAL  78 DAY)),
('10000006', 'DEP',  400.00, DATE_SUB(CURDATE(), INTERVAL 140 DAY)),
('10000007', 'DEP',  500.00, DATE_SUB(CURDATE(), INTERVAL  25 DAY)),
('10000007', 'RET',  300.00, DATE_SUB(CURDATE(), INTERVAL  60 DAY)),
('10000007', 'DEP',  800.00, DATE_SUB(CURDATE(), INTERVAL  70 DAY)),
('10000007', 'RET',   50.00, DATE_SUB(CURDATE(), INTERVAL 115 DAY)),
('10000007', 'DEP',  600.00, DATE_SUB(CURDATE(), INTERVAL 180 DAY));

-- -------------------------------------------------------------
-- Saldo de cada cuenta = depósitos - retiros
-- -------------------------------------------------------------
UPDATE CUENTA c
SET c.SALDO = (SELECT COALESCE(SUM(CASE m.TIPO WHEN 'DEP' THEN m.VALOR ELSE -m.VALOR END), 0)
               FROM MOVIMIENTO m
               WHERE m.NUM_CUENTA = c.NUM_CUENTA);

-- -------------------------------------------------------------
-- Crédito ACTIVO del cliente 5: el ejemplo del enunciado
-- (450.00 a 6 cuotas, 16.5 % anual, cuota fija 78.65) con su tabla
-- de amortización. La última cuota abona todo el saldo como capital
-- (77.59) y el interés absorbe el centavo de redondeo (1.06), así
-- la cuota se mantiene fija y el saldo cierra en 0.00; es el mismo
-- cálculo que hace el servidor Core.
-- -------------------------------------------------------------
INSERT INTO CREDITO (COD_CREDITO, COD_CLIENTE, FECHA, MONTO, NUM_CUOTAS, TASA_ANUAL, VALOR_CUOTA, ESTADO) VALUES
(1, 5, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 450.00, 6, 16.50, 78.65, 'ACTIVO');

INSERT INTO CUOTA (COD_CREDITO, NUM_CUOTA, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO) VALUES
(1, 1, 78.65, 6.19, 72.46, 377.54),
(1, 2, 78.65, 5.19, 73.46, 304.08),
(1, 3, 78.65, 4.18, 74.47, 229.61),
(1, 4, 78.65, 3.16, 75.49, 154.12),
(1, 5, 78.65, 2.12, 76.53,  77.59),
(1, 6, 78.65, 1.06, 77.59,   0.00);

COMMIT;
