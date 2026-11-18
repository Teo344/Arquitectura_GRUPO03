<#
.SYNOPSIS
    Control de las bases de datos en la nube del Grupo 03 (AWS RDS MySQL y SQL Server + tunel SSM).

.DESCRIPTION
    Uso:  .\aws-bdd.ps1 <accion> [base]

      login                 Inicia sesion en AWS desde el navegador (aws login).
      estado                Muestra si la EC2 (puente) y las bases estan encendidas.
      encender [base]       Enciende la EC2 y la base indicada (por defecto, las dos).
      tunel <base>          Abre el tunel de una base: mysql -> 127.0.0.1:3307, sqlserver -> 127.0.0.1,1434.
      apagar   [base]       Apaga la base indicada (por defecto, las dos) y la EC2 si ya no hace falta.

      base = mysql | sqlserver | todas

    Ver GUIA.md en esta misma carpeta.
#>
param(
    [Parameter(Position = 0)]
    [ValidateSet('login', 'estado', 'encender', 'tunel', 'apagar', 'ayuda')]
    [string]$Accion = 'ayuda',

    [Parameter(Position = 1)]
    [ValidateSet('mysql', 'sqlserver', 'todas')]
    [string]$Base = 'todas'
)

# ---- Datos de la infraestructura (solo cambian si se vuelve a crear) ----
$Region     = 'us-east-2'
$InstanceId = 'i-083d8c63fe17abd80'      # EC2 grupo03-puente-bdd

$Bases = [ordered]@{
    mysql = @{
        Nombre     = 'MySQL'
        DbId       = 'grupo03-mysql'
        Endpoint   = 'grupo03-mysql.cduuooaemmbx.us-east-2.rds.amazonaws.com'
        Puerto     = 3306
        Local      = 3307
        ComoConectar = '127.0.0.1 puerto 3307'
    }
    sqlserver = @{
        Nombre     = 'SQL Server'
        DbId       = 'grupo03-mssql'
        Endpoint   = 'grupo03-mssql.cduuooaemmbx.us-east-2.rds.amazonaws.com'
        Puerto     = 1433
        Local      = 1434
        ComoConectar = '127.0.0.1,1434  (host 127.0.0.1, puerto 1434)'
    }
}

function Escribir([string]$Texto, [string]$Color = 'Gray') {
    Write-Host $Texto -ForegroundColor $Color
}

function Probar-Herramientas {
    if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
        Escribir 'No se encuentra AWS CLI. Instalala (GUIA.md, paso 1.2) y abre una terminal nueva.' Red
        exit 1
    }
}

function Probar-Sesion {
    aws sts get-caller-identity --region $Region --query Account --output text 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Escribir 'No hay sesion de AWS o ya expiro. Ejecuta primero:  .\aws-bdd.ps1 login' Red
        exit 1
    }
}

function Claves-Base([string]$Cual) {
    if ($Cual -eq 'todas') { return @($Bases.Keys) }
    return @($Cual)
}

function Estado-EC2 {
    aws ec2 describe-instances --region $Region --instance-ids $InstanceId `
        --query 'Reservations[0].Instances[0].State.Name' --output text
}

function Estado-RDS([string]$DbId) {
    $r = aws rds describe-db-instances --region $Region --db-instance-identifier $DbId `
        --query 'DBInstances[0].DBInstanceStatus' --output text 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $r) { return 'no-existe' }
    return $r
}

function Obtener-Endpoint([string]$Clave) {
    $info = $Bases[$Clave]
    if ($info.Endpoint) { return $info.Endpoint }
    return (aws rds describe-db-instances --region $Region --db-instance-identifier $info.DbId `
        --query 'DBInstances[0].Endpoint.Address' --output text)
}

function Estado-SSM {
    $r = aws ssm describe-instance-information --region $Region `
        --filters "Key=InstanceIds,Values=$InstanceId" `
        --query 'InstanceInformationList[0].PingStatus' --output text
    if (-not $r -or $r -eq 'None') { return 'Offline' }
    return $r
}

function Esta-Lista([string]$Estado) {
    return @('available', 'backing-up') -contains $Estado
}

function Color-Estado([string]$Estado) {
    switch ($Estado) {
        'running'    { return 'Green' }
        'available'  { return 'Green' }
        'backing-up' { return 'Green' }
        'Online'     { return 'Green' }
        'stopped'    { return 'DarkGray' }
        'no-existe'  { return 'Red' }
        default      { return 'Yellow' }
    }
}

function Mostrar-Estado {
    $ec2 = Estado-EC2
    $ssm = Estado-SSM
    Escribir ''
    Escribir ('  EC2 puente        ({0}) : {1}' -f $InstanceId, $ec2) (Color-Estado $ec2)
    Escribir ('  Agente SSM (tunel)                       : {0}' -f $ssm) (Color-Estado $ssm)
    $encendidas = @()
    $apagadas = 0
    foreach ($k in $Bases.Keys) {
        $info = $Bases[$k]
        $e = Estado-RDS $info.DbId
        Escribir ('  {0,-10} ({1,-15})          : {2}' -f $info.Nombre, $info.DbId, $e) (Color-Estado $e)
        if (Esta-Lista $e) { $encendidas += $k }
        if ($e -eq 'stopped') { $apagadas++ }
    }
    Escribir ''
    if ($ec2 -eq 'running' -and $ssm -eq 'Online' -and $encendidas.Count -gt 0) {
        foreach ($k in $encendidas) {
            Escribir ('  {0} lista. Tunel:  .\aws-bdd.ps1 tunel {1}' -f $Bases[$k].Nombre, $k) Green
        }
        Escribir '  Cuando termines (y nadie mas la use):  .\aws-bdd.ps1 apagar' Yellow
    } elseif ($ec2 -eq 'stopped' -and $apagadas -eq $Bases.Count) {
        Escribir '  Todo apagado. No se consume computo.' DarkGray
    } else {
        Escribir '  Algo esta encendiendose o apagandose, o falta encender. Espera un minuto y vuelve a consultar.' Yellow
    }
    Escribir ''
}

function Encender([string]$Cual) {
    $ec2 = Estado-EC2
    if ($ec2 -eq 'stopped') {
        aws ec2 start-instances --region $Region --instance-ids $InstanceId | Out-Null
        Escribir 'EC2: encendiendo...' Yellow
    } elseif ($ec2 -eq 'stopping') {
        Escribir 'La EC2 se esta apagando. Espera 1-2 minutos y vuelve a ejecutar encender.' Yellow
        exit 1
    }

    $claves = Claves-Base $Cual
    foreach ($k in $claves) {
        $info = $Bases[$k]
        $e = Estado-RDS $info.DbId
        if ($e -eq 'stopped') {
            aws rds start-db-instance --region $Region --db-instance-identifier $info.DbId | Out-Null
            Escribir ('{0}: encendiendo (suele tardar entre 5 y 15 minutos)...' -f $info.Nombre) Yellow
        } elseif ($e -eq 'stopping') {
            Escribir ('{0} se esta apagando. Espera unos minutos y vuelve a ejecutar encender.' -f $info.Nombre) Yellow
            exit 1
        } elseif ($e -eq 'no-existe') {
            Escribir ('{0} ({1}) no existe en AWS.' -f $info.Nombre, $info.DbId) Red
            exit 1
        }
    }

    Escribir 'Esperando a la EC2...'
    aws ec2 wait instance-running --region $Region --instance-ids $InstanceId
    foreach ($k in $claves) {
        $info = $Bases[$k]
        Escribir ('Esperando a {0} (no cierres esta ventana)...' -f $info.Nombre)
        aws rds wait db-instance-available --region $Region --db-instance-identifier $info.DbId
    }
    Escribir 'Esperando a que la EC2 quede lista para el tunel...'
    $intentos = 0
    while ((Estado-SSM) -ne 'Online' -and $intentos -lt 24) {
        Start-Sleep -Seconds 10
        $intentos++
    }
    Mostrar-Estado
}

function Abrir-Tunel([string]$Cual) {
    if ($Cual -eq 'todas') {
        Escribir 'Indica que base:  .\aws-bdd.ps1 tunel mysql   o   .\aws-bdd.ps1 tunel sqlserver' Yellow
        Escribir '(Para usar las dos a la vez, abre cada tunel en su propia ventana.)' Yellow
        exit 1
    }
    $info = $Bases[$Cual]
    $ec2 = Estado-EC2
    $rds = Estado-RDS $info.DbId
    if ($ec2 -ne 'running' -or -not (Esta-Lista $rds)) {
        Escribir ("La EC2 esta '{0}' y {1} '{2}'. Primero ejecuta:  .\aws-bdd.ps1 encender {3}" -f $ec2, $info.Nombre, $rds, $Cual) Red
        exit 1
    }
    if (Get-NetTCPConnection -LocalPort $info.Local -State Listen -ErrorAction SilentlyContinue) {
        Escribir ('El puerto {0} ya esta en uso: seguramente ya tienes este tunel abierto en otra ventana.' -f $info.Local) Yellow
        exit 1
    }
    $endpoint = Obtener-Endpoint $Cual
    Escribir ''
    Escribir ('  Tunel hacia {0} ({1})' -f $info.Nombre, $info.DbId) Cyan
    Escribir ('  Conectate a  {0}' -f $info.ComoConectar) Cyan
    Escribir '  Deja ESTA ventana abierta. Para cerrar el tunel: Ctrl + C' Cyan
    Escribir ''
    aws ssm start-session --region $Region --target $InstanceId `
        --document-name AWS-StartPortForwardingSessionToRemoteHost `
        --parameters "host=$endpoint,portNumber=$($info.Puerto),localPortNumber=$($info.Local)"
}

function Apagar([string]$Cual) {
    Escribir 'Antes de apagar, confirma en el grupo que nadie mas esta usando la base.' Yellow
    $respuesta = Read-Host ('Apagar {0}? (s/n)' -f $(if ($Cual -eq 'todas') { 'las dos bases y la EC2' } else { $Bases[$Cual].Nombre }))
    if ($respuesta -notmatch '^[sS]') {
        Escribir 'Cancelado. No se apago nada.'
        return
    }

    foreach ($k in (Claves-Base $Cual)) {
        $info = $Bases[$k]
        $e = Estado-RDS $info.DbId
        if ($e -eq 'available') {
            aws rds stop-db-instance --region $Region --db-instance-identifier $info.DbId | Out-Null
            Escribir ('{0}: apagando...' -f $info.Nombre) Yellow
        } elseif ($e -eq 'stopped' -or $e -eq 'stopping') {
            Escribir ('{0}: ya esta apagada o apagandose.' -f $info.Nombre) DarkGray
        } else {
            Escribir ("{0}: esta en '{1}' y AWS no deja apagarla ahora. Espera unos minutos y ejecuta apagar otra vez." -f $info.Nombre, $e) Red
        }
    }

    # La EC2 solo se apaga si ninguna base queda encendida.
    $siguenEncendidas = @()
    foreach ($k in $Bases.Keys) {
        $e = Estado-RDS $Bases[$k].DbId
        if (@('available', 'backing-up', 'starting', 'modifying') -contains $e) { $siguenEncendidas += $Bases[$k].Nombre }
    }
    $ec2 = Estado-EC2
    if ($siguenEncendidas.Count -gt 0) {
        Escribir ('EC2: se deja encendida porque sigue en uso: {0}.' -f ($siguenEncendidas -join ', ')) Yellow
    } elseif ($ec2 -eq 'running') {
        aws ec2 stop-instances --region $Region --instance-ids $InstanceId | Out-Null
        Escribir 'EC2: apagando...' Yellow
        aws ec2 wait instance-stopped --region $Region --instance-ids $InstanceId
        Escribir 'EC2: apagada.' DarkGray
    } else {
        Escribir "EC2: ya esta '$ec2'." DarkGray
    }

    Escribir ''
    Escribir 'Las bases tardan unos minutos en quedar en "stopped". Compruebalo con:  .\aws-bdd.ps1 estado'
    Escribir 'Ojo: AWS vuelve a encender sola una base apagada a los 7 dias.' Yellow
}

function Mostrar-Ayuda {
    Escribir ''
    Escribir '  Bases de datos en la nube - Grupo 03' Cyan
    Escribir ''
    Escribir '  .\aws-bdd.ps1 login                 Iniciar sesion en AWS (cuando no hay sesion o expiro)'
    Escribir '  .\aws-bdd.ps1 estado                Ver si la EC2 y las bases estan encendidas'
    Escribir '  .\aws-bdd.ps1 encender [base]       Encender (por defecto las dos) y esperar a que esten listas'
    Escribir '  .\aws-bdd.ps1 tunel <base>          Abrir el tunel; dejar la ventana abierta'
    Escribir '                                        mysql     -> 127.0.0.1:3307'
    Escribir '                                        sqlserver -> 127.0.0.1,1434'
    Escribir '  .\aws-bdd.ps1 apagar [base]         Apagar (por defecto las dos) al terminar'
    Escribir ''
    Escribir '  base = mysql | sqlserver | todas'
    Escribir '  Detalle completo en GUIA.md'
    Escribir ''
}

# ---- Programa principal ----
if ($Accion -eq 'ayuda') { Mostrar-Ayuda; exit 0 }

Probar-Herramientas

if ($Accion -eq 'login') {
    aws login --region $Region
    exit $LASTEXITCODE
}

Probar-Sesion

switch ($Accion) {
    'estado'   { Mostrar-Estado }
    'encender' { Encender $Base }
    'tunel'    { Abrir-Tunel $Base }
    'apagar'   { Apagar $Base }
}
