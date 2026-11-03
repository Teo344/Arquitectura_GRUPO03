using System.Collections.Generic;
using System.Runtime.Serialization;
using System.ServiceModel;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Contrato de los tres servicios SOAP publicados por WS_EUREKA_DOTNET_SOAP (AutenticacionWS,
    /// CuentaWS y TransaccionWS), escrito igual que su WSDL. El token viaja en la cabecera SOAP;
    /// lo agrega ClienteWSEureka en cada llamada protegida.
    /// </summary>
    internal static class ContratoWSEureka
    {
        internal const string EspacioNombres = "http://wservicios.controladores.monster.edu.ec/";

        [ServiceContract(Name = "AutenticacionWS", Namespace = EspacioNombres)]
        internal interface Autenticacion
        {
            [OperationContract(Name = "iniciarSesion")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta IniciarSesion(string usuario, string clave, string equipo);

            [OperationContract(Name = "cerrarSesion")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta CerrarSesion();
        }

        [ServiceContract(Name = "CuentaWS", Namespace = EspacioNombres)]
        internal interface Cuenta
        {
            [OperationContract(Name = "consultarCuenta")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta ConsultarCuenta(string cuenta);

            [OperationContract(Name = "consultarMovimientos")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta ConsultarMovimientos(string cuenta);
        }

        [ServiceContract(Name = "TransaccionWS", Namespace = EspacioNombres)]
        internal interface Transaccion
        {
            [OperationContract(Name = "depositar")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta Depositar(string cuenta, decimal? importe);

            [OperationContract(Name = "retirar")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta Retirar(string cuenta, decimal? importe);

            [OperationContract(Name = "transferir")]
            [FaultContract(typeof(ErrorServicio))]
            [return: MessageParameter(Name = "return")]
            Respuesta Transferir(string cuentaOrigen, string cuentaDestino, decimal? importe);
        }

        // Respuesta exitosa de todas las operaciones; los campos que no aplican llegan nulos
        [DataContract(Name = "respuesta", Namespace = EspacioNombres)]
        internal sealed class Respuesta
        {
            [DataMember(Name = "estado", Order = 1, EmitDefaultValue = false)] public string Estado { get; set; }
            [DataMember(Name = "codigo", Order = 2, EmitDefaultValue = false)] public string Codigo { get; set; }
            [DataMember(Name = "mensaje", Order = 3, EmitDefaultValue = false)] public string Mensaje { get; set; }
            [DataMember(Name = "token", Order = 4, EmitDefaultValue = false)] public string Token { get; set; }
            [DataMember(Name = "expiraEn", Order = 5, EmitDefaultValue = false)] public string ExpiraEn { get; set; }
            [DataMember(Name = "codigoEmpleado", Order = 6, EmitDefaultValue = false)] public string CodigoEmpleado { get; set; }
            [DataMember(Name = "nombreEmpleado", Order = 7, EmitDefaultValue = false)] public string NombreEmpleado { get; set; }
            [DataMember(Name = "codigoSucursal", Order = 8, EmitDefaultValue = false)] public string CodigoSucursal { get; set; }
            [DataMember(Name = "sucursal", Order = 9, EmitDefaultValue = false)] public string Sucursal { get; set; }
            [DataMember(Name = "cuenta", Order = 10, EmitDefaultValue = false)] public DatosCuenta Cuenta { get; set; }
            [DataMember(Name = "movimientos", Order = 11, EmitDefaultValue = false)] public List<DatosMovimiento> Movimientos { get; set; }
            [DataMember(Name = "nuevoSaldo", Order = 12, EmitDefaultValue = false)] public decimal? NuevoSaldo { get; set; }
            [DataMember(Name = "numeroMovimiento", Order = 13, EmitDefaultValue = false)] public int? NumeroMovimiento { get; set; }
            [DataMember(Name = "numeroMovimientoDestino", Order = 14, EmitDefaultValue = false)] public int? NumeroMovimientoDestino { get; set; }
        }

        // Datos de la cuenta que devuelve consultarCuenta
        [DataContract(Name = "cuenta", Namespace = EspacioNombres)]
        internal sealed class DatosCuenta
        {
            [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
            [DataMember(Name = "titular", Order = 2)] public string Titular { get; set; }
            [DataMember(Name = "moneda", Order = 3)] public string Moneda { get; set; }
            [DataMember(Name = "saldo", Order = 4)] public decimal Saldo { get; set; }
            [DataMember(Name = "estado", Order = 5)] public string Estado { get; set; }
            [DataMember(Name = "sucursal", Order = 6)] public string Sucursal { get; set; }
            [DataMember(Name = "fechaCreacion", Order = 7)] public string FechaCreacion { get; set; }
        }

        // Movimiento que devuelve consultarMovimientos
        [DataContract(Name = "movimiento", Namespace = EspacioNombres)]
        internal sealed class DatosMovimiento
        {
            [DataMember(Name = "numero", Order = 1)] public int Numero { get; set; }
            [DataMember(Name = "fecha", Order = 2)] public string Fecha { get; set; }
            [DataMember(Name = "tipo", Order = 3)] public string Tipo { get; set; }
            [DataMember(Name = "accion", Order = 4)] public string Accion { get; set; }
            [DataMember(Name = "importe", Order = 5)] public decimal Importe { get; set; }
            [DataMember(Name = "cuentaReferencia", Order = 6, EmitDefaultValue = false)] public string CuentaReferencia { get; set; }
        }

        // Detalle del SOAP Fault: código del ERS (AUTH-001, MOV-002, ...) y mensaje
        [DataContract(Name = "ErrorServicio", Namespace = EspacioNombres)]
        internal sealed class ErrorServicio
        {
            [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
            [DataMember(Name = "mensaje", Order = 2)] public string Mensaje { get; set; }
        }
    }
}
