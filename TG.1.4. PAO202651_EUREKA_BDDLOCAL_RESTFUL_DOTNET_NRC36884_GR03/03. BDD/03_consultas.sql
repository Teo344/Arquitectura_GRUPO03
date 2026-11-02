-- =============================================================
-- EurekaBank - Grupo 03 (NRC 36884)
-- Script 03: consultas de verificación y de referencia (SQL Server)
--
-- No modifica datos. Sirve para comprobar que la BD quedó bien
-- cargada y como referencia de las consultas que hace la capa DAO
-- del servidor (RF-01, RF-03 y RF-04).
-- =============================================================

USE eurekabank;
GO

-- -------------------------------------------------------------
-- 1. Cantidad de registros por tabla
--    Esperado: moneda 2, tipomovimiento 10, sucursal 7, empleado 16,
--    usuario 13, asignado 12, cliente 20, cuenta 6, movimiento 38,
--    logsession 0.
-- -------------------------------------------------------------
SELECT 'moneda' AS tabla, COUNT(*) AS registros FROM moneda
UNION ALL SELECT 'tipomovimiento', COUNT(*) FROM tipomovimiento
UNION ALL SELECT 'sucursal',       COUNT(*) FROM sucursal
UNION ALL SELECT 'empleado',       COUNT(*) FROM empleado
UNION ALL SELECT 'usuario',        COUNT(*) FROM usuario
UNION ALL SELECT 'asignado',       COUNT(*) FROM asignado
UNION ALL SELECT 'cliente',        COUNT(*) FROM cliente
UNION ALL SELECT 'cuenta',         COUNT(*) FROM cuenta
UNION ALL SELECT 'movimiento',     COUNT(*) FROM movimiento
UNION ALL SELECT 'logsession',     COUNT(*) FROM logsession;
GO

-- -------------------------------------------------------------
-- 2. RF-01 Iniciar sesión: usuario MONSTER / MONSTER9
--    Devuelve una fila si las credenciales son correctas y el
--    usuario está ACTIVO. En el servidor, la clave llega como
--    parámetro y se compara su SHA-256 (hexadecimal en minúsculas).
-- -------------------------------------------------------------
DECLARE @usuario VARCHAR(20) = 'MONSTER';
DECLARE @clave   VARCHAR(50) = 'MONSTER9';

SELECT u.chr_emplcodigo                                                   AS codigo_empleado,
       CONCAT(e.vch_emplnombre, ' ', e.vch_emplpaterno, ' ', e.vch_emplmaterno) AS empleado,
       s.chr_sucucodigo                                                   AS codigo_sucursal,
       s.vch_sucunombre                                                   AS sucursal,
       u.vch_emplestado                                                   AS estado
FROM usuario u
JOIN empleado e  ON e.chr_emplcodigo = u.chr_emplcodigo
LEFT JOIN asignado a ON a.chr_emplcodigo = u.chr_emplcodigo AND a.dtt_asigfechabaja IS NULL
LEFT JOIN sucursal s ON s.chr_sucucodigo = a.chr_sucucodigo
WHERE u.vch_emplusuario = @usuario
  AND u.vch_emplclave   = LOWER(CONVERT(CHAR(64), HASHBYTES('SHA2_256', @clave), 2))
  AND u.vch_emplestado  = 'ACTIVO';
GO

-- -------------------------------------------------------------
-- 3. RF-03 Consultar cuenta
-- -------------------------------------------------------------
DECLARE @cuenta CHAR(8) = '00100001';

SELECT c.chr_cuencodigo                                                      AS cuenta,
       CONCAT(cl.vch_clienombre, ' ', cl.vch_cliepaterno, ' ', cl.vch_cliematerno) AS titular,
       m.vch_monedescripcion                                                 AS moneda,
       c.dec_cuensaldo                                                       AS saldo,
       c.vch_cuenestado                                                      AS estado,
       s.vch_sucunombre                                                      AS sucursal,
       c.dtt_cuenfechacreacion                                               AS fecha_creacion
FROM cuenta c
JOIN cliente  cl ON cl.chr_cliecodigo = c.chr_cliecodigo
JOIN moneda   m  ON m.chr_monecodigo  = c.chr_monecodigo
JOIN sucursal s  ON s.chr_sucucodigo  = c.chr_sucucodigo
WHERE c.chr_cuencodigo = @cuenta;

-- -------------------------------------------------------------
-- 4. RF-04 Consultar movimientos de la cuenta
-- -------------------------------------------------------------
SELECT mv.int_movinumero      AS numero,
       mv.dtt_movifecha       AS fecha,
       t.vch_tipodescripcion  AS tipo,
       t.vch_tipoaccion       AS accion,
       mv.dec_moviimporte     AS importe,
       mv.chr_cuenreferencia  AS cuenta_referencia,
       mv.chr_emplcodigo      AS empleado
FROM movimiento mv
JOIN tipomovimiento t ON t.chr_tipocodigo = mv.chr_tipocodigo
WHERE mv.chr_cuencodigo = @cuenta
ORDER BY mv.int_movinumero;
GO

-- -------------------------------------------------------------
-- 5. Consistencia: saldo y contador de cada cuenta frente a sus
--    movimientos. Las columnas "ok_" deben ser 1 en todas las filas.
--    Útil para verificar después de probar depósitos, retiros y
--    transferencias.
-- -------------------------------------------------------------
SELECT c.chr_cuencodigo                                     AS cuenta,
       c.dec_cuensaldo                                      AS saldo,
       COALESCE(SUM(CASE t.vch_tipoaccion
                    WHEN 'INGRESO' THEN mv.dec_moviimporte
                    ELSE -mv.dec_moviimporte END), 0)       AS saldo_calculado,
       c.int_cuencontmov                                    AS contador,
       COALESCE(MAX(mv.int_movinumero), 0)                  AS ultimo_movimiento,
       CASE WHEN c.dec_cuensaldo = COALESCE(SUM(CASE t.vch_tipoaccion
                    WHEN 'INGRESO' THEN mv.dec_moviimporte
                    ELSE -mv.dec_moviimporte END), 0) THEN 1 ELSE 0 END AS ok_saldo,
       CASE WHEN c.int_cuencontmov = COALESCE(MAX(mv.int_movinumero), 0) THEN 1 ELSE 0 END AS ok_contador
FROM cuenta c
LEFT JOIN movimiento mv    ON mv.chr_cuencodigo = c.chr_cuencodigo
LEFT JOIN tipomovimiento t ON t.chr_tipocodigo  = mv.chr_tipocodigo
GROUP BY c.chr_cuencodigo, c.dec_cuensaldo, c.int_cuencontmov
ORDER BY c.chr_cuencodigo;
GO

-- -------------------------------------------------------------
-- 6. Últimas sesiones registradas (RF-01 / RF-02)
--    dtt_logsalida NULL = sesión abierta.
-- -------------------------------------------------------------
SELECT TOP 20 l.int_logid, l.chr_emplcodigo, u.vch_emplusuario,
       l.dtt_logingreso, l.dtt_logsalida, l.vch_logip, l.vch_loghostname
FROM logsession l
JOIN usuario u ON u.chr_emplcodigo = l.chr_emplcodigo
ORDER BY l.int_logid DESC;
GO
