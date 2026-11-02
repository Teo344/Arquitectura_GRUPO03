-- =============================================================
-- BanQuito - Grupo 03 (NRC 36884)
-- Script 03: consultas de verificación y de referencia del Core
--
-- No modifica datos. Comprueba la carga y es la referencia de los
-- SQL que usará el servidor Core para las reglas de crédito:
--   * último mes      = FECHA >= CURDATE() - 1 MES
--   * últimos 3 meses = FECHA >= CURDATE() - 3 MESES
--   * edad            = años cumplidos (TIMESTAMPDIFF)
-- =============================================================

USE banquito;

-- -------------------------------------------------------------
-- 1. Cantidad de registros por tabla
--    Esperado: cliente 6, cuenta 7, movimiento 52, credito 1, cuota 6
-- -------------------------------------------------------------
SELECT 'cliente' AS tabla, COUNT(*) AS registros FROM CLIENTE
UNION ALL SELECT 'cuenta',     COUNT(*) FROM CUENTA
UNION ALL SELECT 'movimiento', COUNT(*) FROM MOVIMIENTO
UNION ALL SELECT 'credito',    COUNT(*) FROM CREDITO
UNION ALL SELECT 'cuota',      COUNT(*) FROM CUOTA;

-- -------------------------------------------------------------
-- 2. ¿Es sujeto de crédito? Las 4 reglas por cliente
--    Esperado: SUJETO_CREDITO = 1 en los clientes 1, 2 y 6;
--    cliente 3 falla R3, cliente 4 falla R2, cliente 5 falla R4.
-- -------------------------------------------------------------
SELECT c.COD_CLIENTE,
       c.CEDULA,
       c.NOMBRE,
       c.GENERO,
       TIMESTAMPDIFF(YEAR, c.FECHA_NACIMIENTO, CURDATE()) AS EDAD,
       r.R2_DEPOSITO_ULTIMO_MES,
       r.R3_EDAD,
       r.R4_SIN_CREDITO_ACTIVO,
       r.R2_DEPOSITO_ULTIMO_MES AND r.R3_EDAD AND r.R4_SIN_CREDITO_ACTIVO AS SUJETO_CREDITO
FROM CLIENTE c
JOIN (
    SELECT c2.COD_CLIENTE,
           EXISTS (SELECT 1
                   FROM MOVIMIENTO m
                   JOIN CUENTA cu ON cu.NUM_CUENTA = m.NUM_CUENTA
                   WHERE cu.COD_CLIENTE = c2.COD_CLIENTE
                     AND m.TIPO = 'DEP'
                     AND m.FECHA >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH))   AS R2_DEPOSITO_ULTIMO_MES,
           NOT (c2.GENERO = 'M'
                AND TIMESTAMPDIFF(YEAR, c2.FECHA_NACIMIENTO, CURDATE()) < 25) AS R3_EDAD,
           NOT EXISTS (SELECT 1
                       FROM CREDITO cr
                       WHERE cr.COD_CLIENTE = c2.COD_CLIENTE
                         AND cr.ESTADO = 'ACTIVO')                           AS R4_SIN_CREDITO_ACTIVO
    FROM CLIENTE c2
) r ON r.COD_CLIENTE = c.COD_CLIENTE
ORDER BY c.COD_CLIENTE;

-- Regla 1 para una cédula: 1 si es cliente, 0 si no.
-- Esperado: 9999999999 -> 0; 1712345678 -> 1.
SET @cedula = '9999999999';
SELECT @cedula AS CEDULA, COUNT(*) > 0 AS R1_ES_CLIENTE FROM CLIENTE WHERE CEDULA = @cedula;

-- -------------------------------------------------------------
-- 3. Monto máximo de crédito por cliente (últimos 3 meses)
--    ((promedio depósitos - promedio retiros) * 30%) * 6, mínimo 0.
--    Esperado: 1 -> 2250.00, 2 -> 450.00, 6 -> 945.00
--    (3 -> 990.00, 4 -> 1125.00, 5 -> 1305.00, aunque no son sujetos de crédito)
-- -------------------------------------------------------------
SELECT c.COD_CLIENTE,
       c.NOMBRE,
       ROUND(AVG(CASE WHEN m.TIPO = 'DEP' THEN m.VALOR END), 2) AS PROMEDIO_DEPOSITOS,
       ROUND(AVG(CASE WHEN m.TIPO = 'RET' THEN m.VALOR END), 2) AS PROMEDIO_RETIROS,
       ROUND(GREATEST((COALESCE(AVG(CASE WHEN m.TIPO = 'DEP' THEN m.VALOR END), 0)
                     - COALESCE(AVG(CASE WHEN m.TIPO = 'RET' THEN m.VALOR END), 0)) * 0.30 * 6, 0), 2) AS MONTO_MAXIMO
FROM CLIENTE c
LEFT JOIN CUENTA cu    ON cu.COD_CLIENTE = c.COD_CLIENTE
LEFT JOIN MOVIMIENTO m ON m.NUM_CUENTA = cu.NUM_CUENTA
                      AND m.FECHA >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
GROUP BY c.COD_CLIENTE, c.NOMBRE
ORDER BY c.COD_CLIENTE;

-- -------------------------------------------------------------
-- 4. Tabla de amortización de un crédito (cliente 5)
--    Esperado: la tabla de ejemplo del enunciado (450 a 6 cuotas).
-- -------------------------------------------------------------
SELECT cr.COD_CREDITO, cr.COD_CLIENTE, cr.MONTO, cr.NUM_CUOTAS, cr.TASA_ANUAL, cr.VALOR_CUOTA, cr.ESTADO,
       q.NUM_CUOTA, q.VALOR_CUOTA AS CUOTA, q.INTERES_PAGADO, q.CAPITAL_PAGADO, q.SALDO
FROM CREDITO cr
JOIN CUOTA q ON q.COD_CREDITO = cr.COD_CREDITO
WHERE cr.COD_CLIENTE = 5
ORDER BY q.NUM_CUOTA;

-- -------------------------------------------------------------
-- 5. Consistencia: saldo de cada cuenta frente a sus movimientos.
--    La columna ok_saldo debe ser 1 en todas las filas.
-- -------------------------------------------------------------
SELECT cu.NUM_CUENTA,
       cu.COD_CLIENTE,
       cu.SALDO,
       COALESCE(SUM(CASE m.TIPO WHEN 'DEP' THEN m.VALOR ELSE -m.VALOR END), 0) AS SALDO_CALCULADO,
       cu.SALDO = COALESCE(SUM(CASE m.TIPO WHEN 'DEP' THEN m.VALOR ELSE -m.VALOR END), 0) AS ok_saldo
FROM CUENTA cu
LEFT JOIN MOVIMIENTO m ON m.NUM_CUENTA = cu.NUM_CUENTA
GROUP BY cu.NUM_CUENTA, cu.COD_CLIENTE, cu.SALDO
ORDER BY cu.NUM_CUENTA;
