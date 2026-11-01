-- =============================================================
-- EurekaBank - Grupo 03 (NRC 36884)
-- Script 02: datos de trabajo
--
-- Datos de la BD base de EurekaBank (entrega GR08), filtrados a
-- las 10 tablas del sistema. Los saldos de las 6 cuentas
-- coinciden con la suma de sus movimientos.
--
-- Usuario principal para iniciar sesión:  MONSTER / MONSTER9
-- Las claves se guardan con SHA2(clave, 256).
-- =============================================================

USE eurekabank;

-- -------------------------------------------------------------
-- Moneda
-- -------------------------------------------------------------
INSERT INTO moneda VALUES ('01', 'Soles');
INSERT INTO moneda VALUES ('02', 'Dolares');

-- -------------------------------------------------------------
-- Tipos de movimiento
-- El sistema genera 003, 004, 008 y 009; los demás se conservan
-- porque aparecen en el historial de movimientos.
-- -------------------------------------------------------------
INSERT INTO tipomovimiento VALUES ('001', 'Apertura de Cuenta',   'INGRESO', 'ACTIVO');
INSERT INTO tipomovimiento VALUES ('002', 'Cancelar Cuenta',      'SALIDA',  'ACTIVO');
INSERT INTO tipomovimiento VALUES ('003', 'Deposito',             'INGRESO', 'ACTIVO');
INSERT INTO tipomovimiento VALUES ('004', 'Retiro',               'SALIDA',  'ACTIVO');
INSERT INTO tipomovimiento VALUES ('005', 'Interes',              'INGRESO', 'ACTIVO');
INSERT INTO tipomovimiento VALUES ('006', 'Mantenimiento',        'SALIDA',  'ACTIVO');
INSERT INTO tipomovimiento VALUES ('007', 'ITF',                  'SALIDA',  'ACTIVO');
INSERT INTO tipomovimiento VALUES ('008', 'Transferencia',        'INGRESO', 'ACTIVO');
INSERT INTO tipomovimiento VALUES ('009', 'Transferencia',        'SALIDA',  'ACTIVO');
INSERT INTO tipomovimiento VALUES ('010', 'Cargo por Movimiento', 'SALIDA',  'ACTIVO');

-- -------------------------------------------------------------
-- Sucursal (int_sucucontcuenta = cuentas creadas en la sucursal)
-- -------------------------------------------------------------
INSERT INTO sucursal VALUES ('001', 'Sipan',       'Chiclayo', 'Av. Balta 1456',             2);
INSERT INTO sucursal VALUES ('002', 'Chan Chan',   'Trujillo', 'Jr. Independencia 456',      3);
INSERT INTO sucursal VALUES ('003', 'Los Olivos',  'Lima',     'Av. Central 1234',           1);
INSERT INTO sucursal VALUES ('004', 'Pardo',       'Lima',     'Av. Pardo 345 - Miraflores', 0);
INSERT INTO sucursal VALUES ('005', 'Misti',       'Arequipa', 'Bolivar 546',                0);
INSERT INTO sucursal VALUES ('006', 'Machupicchu', 'Cusco',    'Calle El Sol 534',           0);
INSERT INTO sucursal VALUES ('007', 'Grau',        'Piura',    'Av. Grau 1528',              0);

-- -------------------------------------------------------------
-- Empleado
-- 0015 es el empleado del grupo asociado al usuario MONSTER.
-- -------------------------------------------------------------
INSERT INTO empleado VALUES ('9999', 'Internet',  'Internet',  'Internet',        'Internet', 'Internet');
INSERT INTO empleado VALUES ('0001', 'Romero',    'Castillo',  'Carlos Alberto',  'Trujillo', 'Calle 1 Nro. 456');
INSERT INTO empleado VALUES ('0002', 'Castro',    'Vargas',    'Lidia',           'Lima',     'Federico Villarreal 456 - SMP');
INSERT INTO empleado VALUES ('0003', 'Reyes',     'Ortiz',     'Claudia',         'Lima',     'Av. Aviación 3456 - San Borja');
INSERT INTO empleado VALUES ('0004', 'Ramos',     'Garibay',   'Angelica',        'Chiclayo', 'Calle Barcelona 345');
INSERT INTO empleado VALUES ('0005', 'Ruiz',      'Zabaleta',  'Claudia',         'Cusco',    'Calle Cruz Verde 364');
INSERT INTO empleado VALUES ('0006', 'Cruz',      'Tarazona',  'Ricardo',         'Arequipa', 'Calle La Gruta 304');
INSERT INTO empleado VALUES ('0007', 'Diaz',      'Flores',    'Edith',           'Lima',     'Av. Pardo 546');
INSERT INTO empleado VALUES ('0008', 'Sarmiento', 'Bellido',   'Claudia Rocio',   'Arequipa', 'Calle Alfonso Ugarte 1567');
INSERT INTO empleado VALUES ('0009', 'Pachas',    'Sifuentes', 'Luis Alberto',    'Trujillo', 'Francisco Pizarro 1263');
INSERT INTO empleado VALUES ('0010', 'Tello',     'Alarcon',   'Hugo Valentin',   'Cusco',    'Los Angeles 865');
INSERT INTO empleado VALUES ('0011', 'Carrasco',  'Vargas',    'Pedro Hugo',      'Chiclayo', 'Av. Balta 1265');
INSERT INTO empleado VALUES ('0012', 'Mendoza',   'Jara',      'Monica Valeria',  'Lima',     'Calle Las Toronjas 450');
INSERT INTO empleado VALUES ('0013', 'Espinoza',  'Melgar',    'Victor Eduardo',  'Huancayo', 'Av. San Martin 6734 Dpto. 508');
INSERT INTO empleado VALUES ('0014', 'Hidalgo',   'Sandoval',  'Milagros Leonor', 'Chiclayo', 'Av. Luis Gonzales 1230');
INSERT INTO empleado VALUES ('0015', 'Monster',   'Grupo03',   'Operador',        'Quito',    'Av. General Rumiñahui s/n');

-- -------------------------------------------------------------
-- Usuario (clave = SHA-256 en hexadecimal)
-- MONSTER es el usuario principal del sistema. Los demás se
-- conservan para probar varios cajeros y el error AUTH-002
-- (usuarios ANULADO: creyes, ediaz, csarmiento).
-- -------------------------------------------------------------
INSERT INTO usuario VALUES ('0015', 'MONSTER',    SHA2('MONSTER9', 256),   'ACTIVO');
INSERT INTO usuario VALUES ('9999', 'internet',   SHA2('internet', 256),   'ACTIVO');
INSERT INTO usuario VALUES ('0001', 'cromero',    SHA2('chicho', 256),     'ACTIVO');
INSERT INTO usuario VALUES ('0002', 'lcastro',    SHA2('flaca', 256),      'ACTIVO');
INSERT INTO usuario VALUES ('0003', 'creyes',     SHA2('linda', 256),      'ANULADO');
INSERT INTO usuario VALUES ('0004', 'aramos',     SHA2('china', 256),      'ACTIVO');
INSERT INTO usuario VALUES ('0005', 'cvalencia',  SHA2('angel', 256),      'ACTIVO');
INSERT INTO usuario VALUES ('0006', 'rcruz',      SHA2('cerebro', 256),    'ACTIVO');
INSERT INTO usuario VALUES ('0007', 'ediaz',      SHA2('princesa', 256),   'ANULADO');
INSERT INTO usuario VALUES ('0008', 'csarmiento', SHA2('chinita', 256),    'ANULADO');
INSERT INTO usuario VALUES ('0009', 'lpachas',    SHA2('gato', 256),       'ACTIVO');
INSERT INTO usuario VALUES ('0010', 'htello',     SHA2('machupichu', 256), 'ACTIVO');
INSERT INTO usuario VALUES ('0011', 'pcarrasco',  SHA2('tinajones', 256),  'ACTIVO');

-- -------------------------------------------------------------
-- Asignado (sucursal vigente de cada empleado)
-- -------------------------------------------------------------
INSERT INTO asignado VALUES ('000001', '001', '0004', '2007-11-15', NULL);
INSERT INTO asignado VALUES ('000002', '002', '0001', '2007-11-20', NULL);
INSERT INTO asignado VALUES ('000003', '003', '0002', '2007-11-28', NULL);
INSERT INTO asignado VALUES ('000004', '004', '0003', '2007-12-12', '2008-03-25');
INSERT INTO asignado VALUES ('000005', '005', '0006', '2007-12-20', NULL);
INSERT INTO asignado VALUES ('000006', '006', '0005', '2008-01-05', NULL);
INSERT INTO asignado VALUES ('000007', '004', '0007', '2008-01-07', NULL);
INSERT INTO asignado VALUES ('000008', '005', '0008', '2008-01-07', NULL);
INSERT INTO asignado VALUES ('000009', '001', '0011', '2008-01-08', NULL);
INSERT INTO asignado VALUES ('000010', '002', '0009', '2008-01-08', NULL);
INSERT INTO asignado VALUES ('000011', '006', '0010', '2008-01-08', NULL);
INSERT INTO asignado VALUES ('000012', '001', '0015', '2026-11-01', NULL);

-- -------------------------------------------------------------
-- Cliente
-- -------------------------------------------------------------
INSERT INTO cliente VALUES ('00001', 'CORONEL',    'CASTILLO',   'ERIC GUSTAVO',      '06914897', 'LIMA', 'LOS OLIVOS',   '996-664-457', 'gcoronelc@gmail.com');
INSERT INTO cliente VALUES ('00002', 'VALENCIA',   'MORALES',    'PEDRO HUGO',        '01576173', 'LIMA', 'MAGDALENA',    '924-7834',    'pvalencia@terra.com.pe');
INSERT INTO cliente VALUES ('00003', 'MARCELO',    'VILLALOBOS', 'RICARDO',           '10762367', 'LIMA', 'LINCE',        '993-62966',   'ricardomarcelo@hotmail.com');
INSERT INTO cliente VALUES ('00004', 'ROMERO',     'CASTILLO',   'CARLOS ALBERTO',    '06531983', 'LIMA', 'LOS OLIVOS',   '865-84762',   'c.romero@hotmail.com');
INSERT INTO cliente VALUES ('00005', 'ARANDA',     'LUNA',       'ALAN ALBERTO',      '10875611', 'LIMA', 'SAN ISIDRO',   '834-67125',   'a.aranda@hotmail.com');
INSERT INTO cliente VALUES ('00006', 'AYALA',      'PAZ',        'JORGE LUIS',        '10679245', 'LIMA', 'SAN BORJA',    '963-34769',   'j.ayala@yahoo.com');
INSERT INTO cliente VALUES ('00007', 'CHAVEZ',     'CANALES',    'EDGAR RAFAEL',      '10145693', 'LIMA', 'MIRAFLORES',   '999-96673',   'e.chavez@gmail.com');
INSERT INTO cliente VALUES ('00008', 'FLORES',     'CHAFLOQUE',  'ROSA LIZET',        '10773456', 'LIMA', 'LA MOLINA',    '966-87567',   'r.florez@hotmail.com');
INSERT INTO cliente VALUES ('00009', 'FLORES',     'CASTILLO',   'CRISTIAN RAFAEL',   '10346723', 'LIMA', 'LOS OLIVOS',   '978-43768',   'c.flores@hotmail.com');
INSERT INTO cliente VALUES ('00010', 'GONZALES',   'GARCIA',     'GABRIEL ALEJANDRO', '10192376', 'LIMA', 'SAN MIGUEL',   '945-56782',   'g.gonzales@yahoo.es');
INSERT INTO cliente VALUES ('00011', 'LAY',        'VALLEJOS',   'JUAN CARLOS',       '10942287', 'LIMA', 'LINCE',        '956-12657',   'j.lay@peru.com');
INSERT INTO cliente VALUES ('00012', 'MONTALVO',   'SOTO',       'DEYSI LIDIA',       '10612376', 'LIMA', 'SURCO',        '965-67235',   'd.montalvo@hotmail.com');
INSERT INTO cliente VALUES ('00013', 'RICALDE',    'RAMIREZ',    'ROSARIO ESMERALDA', '10761324', 'LIMA', 'MIRAFLORES',   '991-23546',   'r.ricalde@gmail.com');
INSERT INTO cliente VALUES ('00014', 'RODRIGUEZ',  'FLORES',     'ENRIQUE MANUEL',    '10773345', 'LIMA', 'LINCE',        '976-82838',   'e.rodriguez@gmail.com');
INSERT INTO cliente VALUES ('00015', 'ROJAS',      'OSCANOA',    'FELIX NINO',        '10238943', 'LIMA', 'LIMA',         '962-32158',   'f.rojas@yahoo.com');
INSERT INTO cliente VALUES ('00016', 'TEJADA',     'DEL AGUILA', 'TANIA LORENA',      '10446791', 'LIMA', 'PUEBLO LIBRE', '966-23854',   't.tejada@hotmail.com');
INSERT INTO cliente VALUES ('00017', 'VALDEVIESO', 'LEYVA',      'LIDIA ROXANA',      '10452682', 'LIMA', 'SURCO',        '956-78951',   'r.valdivieso@terra.com.pe');
INSERT INTO cliente VALUES ('00018', 'VALENTIN',   'COTRINA',    'JUAN DIEGO',        '10398247', 'LIMA', 'LA MOLINA',    '921-12456',   'j.valentin@terra.com.pe');
INSERT INTO cliente VALUES ('00019', 'YAURICASA',  'BAUTISTA',   'YESABETH',          '10934584', 'LIMA', 'MAGDALENA',    '977-75777',   'y.yauricasa@terra.com.pe');
INSERT INTO cliente VALUES ('00020', 'ZEGARRA',    'GARCIA',     'FERNANDO MOISES',   '10772365', 'LIMA', 'SAN ISIDRO',   '936-45876',   'f.zegarra@hotmail.com');

-- -------------------------------------------------------------
-- Cuenta (int_cuencontmov = número del último movimiento)
-- -------------------------------------------------------------
INSERT INTO cuenta VALUES ('00200001', '01', '002', '0001', '00008', 7000.00, '2008-01-05', 'ACTIVO',    15, '123456');
INSERT INTO cuenta VALUES ('00200002', '01', '002', '0001', '00001', 6800.00, '2008-01-09', 'ACTIVO',     3, '123456');
INSERT INTO cuenta VALUES ('00200003', '02', '002', '0001', '00007', 6000.00, '2008-01-11', 'ACTIVO',     6, '123456');
INSERT INTO cuenta VALUES ('00100001', '01', '001', '0004', '00005', 6900.00, '2008-01-06', 'ACTIVO',     7, '123456');
INSERT INTO cuenta VALUES ('00100002', '02', '001', '0004', '00005', 4500.00, '2008-01-08', 'ACTIVO',     4, '123456');
INSERT INTO cuenta VALUES ('00300001', '01', '003', '0002', '00010',    0.00, '2008-01-07', 'CANCELADO',  3, '123456');

-- -------------------------------------------------------------
-- Movimiento
-- -------------------------------------------------------------
INSERT INTO movimiento VALUES ('00100002',  1, '2008-01-08', '0004', '001', 1800.00, NULL);
INSERT INTO movimiento VALUES ('00100002',  2, '2008-01-25', '0004', '004', 1000.00, NULL);
INSERT INTO movimiento VALUES ('00100002',  3, '2008-02-13', '0004', '003', 2200.00, NULL);
INSERT INTO movimiento VALUES ('00100002',  4, '2008-03-08', '0004', '003', 1500.00, NULL);

INSERT INTO movimiento VALUES ('00100001',  1, '2008-01-06', '0004', '001', 2800.00, NULL);
INSERT INTO movimiento VALUES ('00100001',  2, '2008-01-15', '0004', '003', 3200.00, NULL);
INSERT INTO movimiento VALUES ('00100001',  3, '2008-01-20', '0004', '004',  800.00, NULL);
INSERT INTO movimiento VALUES ('00100001',  4, '2008-02-14', '0004', '003', 2000.00, NULL);
INSERT INTO movimiento VALUES ('00100001',  5, '2008-02-25', '0004', '004',  500.00, NULL);
INSERT INTO movimiento VALUES ('00100001',  6, '2008-03-03', '0004', '004',  800.00, NULL);
INSERT INTO movimiento VALUES ('00100001',  7, '2008-03-15', '0004', '003', 1000.00, NULL);

INSERT INTO movimiento VALUES ('00200003',  1, '2008-01-11', '0001', '001', 2500.00, NULL);
INSERT INTO movimiento VALUES ('00200003',  2, '2008-01-17', '0001', '003', 1500.00, NULL);
INSERT INTO movimiento VALUES ('00200003',  3, '2008-01-20', '0001', '004',  500.00, NULL);
INSERT INTO movimiento VALUES ('00200003',  4, '2008-02-09', '0001', '004',  500.00, NULL);
INSERT INTO movimiento VALUES ('00200003',  5, '2008-02-25', '0001', '003', 3500.00, NULL);
INSERT INTO movimiento VALUES ('00200003',  6, '2008-03-11', '0001', '004',  500.00, NULL);

INSERT INTO movimiento VALUES ('00200002',  1, '2008-01-09', '0001', '001', 3800.00, NULL);
INSERT INTO movimiento VALUES ('00200002',  2, '2008-01-20', '0001', '003', 4200.00, NULL);
INSERT INTO movimiento VALUES ('00200002',  3, '2008-03-06', '0001', '004', 1200.00, NULL);

INSERT INTO movimiento VALUES ('00200001',  1, '2008-01-05', '0001', '001', 5000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  2, '2008-01-07', '0001', '003', 4000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  3, '2008-01-09', '0001', '004', 2000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  4, '2008-01-11', '0001', '003', 1000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  5, '2008-01-13', '0001', '003', 2000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  6, '2008-01-15', '0001', '004', 4000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  7, '2008-01-19', '0001', '003', 2000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  8, '2008-01-21', '0001', '004', 3000.00, NULL);
INSERT INTO movimiento VALUES ('00200001',  9, '2008-01-23', '0001', '003', 7000.00, NULL);
INSERT INTO movimiento VALUES ('00200001', 10, '2008-01-27', '0001', '004', 1000.00, NULL);
INSERT INTO movimiento VALUES ('00200001', 11, '2008-01-30', '0001', '004', 3000.00, NULL);
INSERT INTO movimiento VALUES ('00200001', 12, '2008-02-04', '0001', '003', 2000.00, NULL);
INSERT INTO movimiento VALUES ('00200001', 13, '2008-02-08', '0001', '004', 4000.00, NULL);
INSERT INTO movimiento VALUES ('00200001', 14, '2008-02-13', '0001', '003', 2000.00, NULL);
INSERT INTO movimiento VALUES ('00200001', 15, '2008-02-19', '0001', '004', 1000.00, NULL);

INSERT INTO movimiento VALUES ('00300001',  1, '2008-01-07', '0002', '001', 5600.00, NULL);
INSERT INTO movimiento VALUES ('00300001',  2, '2008-01-18', '0002', '003', 1400.00, NULL);
INSERT INTO movimiento VALUES ('00300001',  3, '2008-01-25', '0002', '002', 7000.00, NULL);

COMMIT;
