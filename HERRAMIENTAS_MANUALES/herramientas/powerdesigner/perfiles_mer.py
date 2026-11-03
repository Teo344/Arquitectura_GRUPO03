"""Datos de cada base de datos para generar_specs_mer.py: los que el script SQL no tiene.
  TABLAS:     tabla -> (nombre legible, comentario, columna, fila) para el comentario y la posición en el diagrama
  COLUMNAS:   'TABLA.COLUMNA' o 'COLUMNA' -> descripción (comentario de la columna)
  RELACIONES: FK -> (verbo leído del padre a la hija, cardinalidad del lado hijo)"""

PERFILES = {
    # EurekaBank (TG.1.1 a TG.1.4)
    'eureka': {
        'TABLAS': {
            'usuario':        ('Usuario', 'Credenciales de acceso del empleado (clave SHA-256).', 0, 0),
            'empleado':       ('Empleado', 'Empleado (cajero) que opera en ventanilla.', 1, 0),
            'asignado':       ('Asignado', 'Asignación vigente o histórica del empleado a una sucursal.', 2, 0),
            'sucursal':       ('Sucursal', 'Sucursal del banco.', 3, 0),
            'logsession':     ('Sesión', 'Registro de ingreso (RF-01) y salida (RF-02) de cada sesión.', 0, 1),
            'movimiento':     ('Movimiento', 'Movimiento de una cuenta: depósito, retiro o transferencia.', 1, 1),
            'cuenta':         ('Cuenta', 'Cuenta bancaria del cliente.', 2, 1),
            'moneda':         ('Moneda', 'Catálogo de monedas.', 3, 1),
            'tipomovimiento': ('Tipo de movimiento', 'Catálogo de tipos de movimiento (INGRESO o SALIDA).', 1, 2),
            'cliente':        ('Cliente', 'Titular de las cuentas.', 2, 2),
        },
        'COLUMNAS': {
            'chr_monecodigo': 'Código de moneda', 'vch_monedescripcion': 'Descripción de moneda',
            'chr_tipocodigo': 'Código de tipo', 'vch_tipodescripcion': 'Descripción de tipo', 'vch_tipoaccion': 'Acción',
            'vch_tipoestado': 'Estado de tipo',
            'chr_sucucodigo': 'Código de sucursal', 'vch_sucunombre': 'Nombre de sucursal',
            'vch_sucuciudad': 'Ciudad de sucursal', 'vch_sucudireccion': 'Dirección de sucursal',
            'int_sucucontcuenta': 'Contador de cuentas',
            'chr_emplcodigo': 'Código de empleado', 'vch_emplpaterno': 'Apellido paterno de empleado',
            'vch_emplmaterno': 'Apellido materno de empleado', 'vch_emplnombre': 'Nombre de empleado',
            'vch_emplciudad': 'Ciudad de empleado', 'vch_empldireccion': 'Dirección de empleado',
            'vch_emplusuario': 'Usuario', 'vch_emplclave': 'Clave', 'vch_emplestado': 'Estado de usuario',
            'chr_asigcodigo': 'Código de asignación', 'dtt_asigfechaalta': 'Fecha de alta',
            'dtt_asigfechabaja': 'Fecha de baja',
            'int_logid': 'Id de sesión', 'dtt_logingreso': 'Fecha de ingreso', 'dtt_logsalida': 'Fecha de salida',
            'vch_logip': 'Dirección IP', 'vch_loghostname': 'Nombre del equipo',
            'chr_cliecodigo': 'Código de cliente', 'vch_cliepaterno': 'Apellido paterno de cliente',
            'vch_cliematerno': 'Apellido materno de cliente', 'vch_clienombre': 'Nombre de cliente', 'chr_cliedni': 'DNI',
            'vch_clieciudad': 'Ciudad de cliente', 'vch_cliedireccion': 'Dirección de cliente',
            'vch_clietelefono': 'Teléfono', 'vch_clieemail': 'Correo electrónico',
            'chr_cuencodigo': 'Número de cuenta', 'chr_emplcreacuenta': 'Empleado que crea la cuenta',
            'dec_cuensaldo': 'Saldo', 'dtt_cuenfechacreacion': 'Fecha de creación', 'vch_cuenestado': 'Estado de cuenta',
            'int_cuencontmov': 'Contador de movimientos', 'chr_cuenclave': 'Clave de cuenta',
            'int_movinumero': 'Número de movimiento', 'dtt_movifecha': 'Fecha de movimiento', 'dec_moviimporte': 'Importe',
            'chr_cuenreferencia': 'Cuenta de referencia',
        },
        'RELACIONES': {
            'fk_usuario_empleado':      ('Tiene', '0..1'),
            'fk_asignado_empleado':     ('Es asignado', '0..*'),
            'fk_asignado_sucursal':     ('Recibe', '0..*'),
            'fk_logsession_empleado':   ('Inicia', '0..*'),
            'fk_cuenta_moneda':         ('Denomina', '0..*'),
            'fk_cuenta_sucursal':       ('Administra', '0..*'),
            'fk_cuenta_empleado':       ('Crea', '0..*'),
            'fk_cuenta_cliente':        ('Posee', '0..*'),
            'fk_movimiento_cuenta':     ('Registra', '0..*'),
            'fk_movimiento_empleado':   ('Realiza', '0..*'),
            'fk_movimiento_tipo':       ('Clasifica', '0..*'),
            'fk_movimiento_referencia': ('Es referencia de', '0..*'),
        },
    },

    # BanQuito, Core bancario y módulo de crédito (EX.1.1)
    'banquito': {
        'TABLAS': {
            'CUENTA':     ('Cuenta', 'Cuenta de ahorros del cliente.', 0, 0),
            'CLIENTE':    ('Cliente', 'Cliente del banco; la cédula es única.', 1, 0),
            'CREDITO':    ('Crédito', 'Crédito de cuota fija otorgado al cliente (tasa anual 16.5 %).', 2, 0),
            'MOVIMIENTO': ('Movimiento', 'Depósito (DEP) o retiro (RET) de una cuenta.', 0, 1),
            'CUOTA':      ('Cuota', 'Fila de la tabla de amortización del crédito.', 2, 1),
        },
        'COLUMNAS': {
            'COD_CLIENTE': 'Código de cliente', 'CEDULA': 'Cédula', 'NOMBRE': 'Nombre del cliente',
            'GENERO': 'Género (M o F)', 'FECHA_NACIMIENTO': 'Fecha de nacimiento',
            'NUM_CUENTA': 'Número de cuenta', 'CUENTA.SALDO': 'Saldo de la cuenta',
            'COD_MOVIMIENTO': 'Código de movimiento', 'TIPO': 'Tipo (DEP o RET)', 'VALOR': 'Valor del movimiento',
            'MOVIMIENTO.FECHA': 'Fecha del movimiento',
            'COD_CREDITO': 'Código de crédito', 'CREDITO.FECHA': 'Fecha de otorgamiento', 'MONTO': 'Monto del crédito',
            'NUM_CUOTAS': 'Número de cuotas (3 a 18)', 'TASA_ANUAL': 'Tasa de interés anual',
            'CREDITO.VALOR_CUOTA': 'Valor de la cuota fija', 'ESTADO': 'Estado del crédito',
            'NUM_CUOTA': 'Número de cuota', 'CUOTA.VALOR_CUOTA': 'Valor de la cuota',
            'INTERES_PAGADO': 'Interés pagado', 'CAPITAL_PAGADO': 'Capital pagado',
            'CUOTA.SALDO': 'Saldo del crédito después de la cuota',
        },
        'RELACIONES': {
            'FK_CUENTA_CLIENTE':    ('Posee', '0..*'),
            'FK_MOVIMIENTO_CUENTA': ('Registra', '0..*'),
            'FK_CREDITO_CLIENTE':   ('Obtiene', '0..*'),
            'FK_CUOTA_CREDITO':     ('Se amortiza en', '0..*'),
        },
    },

    # Comercializadora de celulares, facturación (EX.1.1)
    'comercializadora': {
        'TABLAS': {
            'USUARIO':         ('Usuario', 'Empleado que inicia sesión (clave SHA-256).', 0, 0),
            'FACTURA':         ('Factura', 'Venta en efectivo (42 % de descuento) o a crédito del banco.', 1, 0),
            'DETALLE_FACTURA': ('Detalle de factura', 'Teléfono vendido en la factura.', 1, 1),
            'TELEFONO':        ('Teléfono', 'Catálogo de teléfonos; eliminar lo deja INACTIVO.', 2, 1),
        },
        'COLUMNAS': {
            'COD_USUARIO': 'Código de usuario', 'USUARIO': 'Usuario (login)', 'CLAVE': 'Clave', 'NOMBRE': 'Nombre',
            'USUARIO.ESTADO': 'Estado del usuario',
            'COD_TELEFONO': 'Código de teléfono', 'MARCA': 'Marca', 'MODELO': 'Modelo', 'PRECIO': 'Precio',
            'TELEFONO.ESTADO': 'Estado en el catálogo',
            'NUM_FACTURA': 'Número de factura', 'FECHA': 'Fecha y hora de la venta',
            'CEDULA_CLIENTE': 'Cédula del comprador', 'COD_CLIENTE_BANCO': 'Código del comprador en el banco',
            'NOMBRE_CLIENTE': 'Nombre del comprador', 'FORMA_PAGO': 'Forma de pago (EFECTIVO o CREDITO)',
            'FACTURA.SUBTOTAL': 'Subtotal de la factura', 'DESCUENTO': 'Descuento', 'TOTAL': 'Total',
            'COD_CREDITO_BANCO': 'Crédito otorgado por el banco', 'NUM_CUOTAS': 'Número de cuotas del crédito',
            'CANTIDAD': 'Cantidad', 'PRECIO_UNITARIO': 'Precio unitario',
            'DETALLE_FACTURA.SUBTOTAL': 'Subtotal de la línea',
        },
        'RELACIONES': {
            'FK_FACTURA_USUARIO':  ('Emite', '0..*'),
            'FK_DETALLE_FACTURA':  ('Contiene', '0..*'),
            'FK_DETALLE_TELEFONO': ('Se vende en', '0..*'),
        },
    },
}
