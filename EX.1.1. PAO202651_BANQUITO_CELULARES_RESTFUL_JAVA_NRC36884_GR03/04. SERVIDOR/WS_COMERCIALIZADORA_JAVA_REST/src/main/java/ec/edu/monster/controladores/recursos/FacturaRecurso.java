package ec.edu.monster.controladores.recursos;

import ec.edu.monster.modelo.Factura;
import ec.edu.monster.seguridad.ManejadorAutenticacion;
import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.seguridad.Sesion;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioFacturacion;
import ec.edu.monster.servicios.ServicioFacturacion.Linea;
import ec.edu.monster.vista.FacturaDto;
import ec.edu.monster.vista.Mapeador;
import ec.edu.monster.vista.SolicitudFacturaDto;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Ventas: POST /facturas vende en efectivo o a crédito (201 + Location) y GET
 * consulta una factura o la lista. El empleado que factura sale de la sesión
 * del token, nunca del cuerpo de la petición.
 *
 * @author carlo
 */
@Path("facturas")
@Protegido
@Produces(MediaType.APPLICATION_JSON)
public class FacturaRecurso {

    @Inject
    private ServicioFacturacion servicioFacturacion;

    @Inject
    private Mapeador mapeador;

    @Context
    private HttpServletRequest peticion;

    @Context
    private UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response facturar(SolicitudFacturaDto solicitud) throws ExcepcionServicio {
        SolicitudFacturaDto s = solicitud != null ? solicitud : new SolicitudFacturaDto();
        List<Linea> lineas = s.detalles == null ? null
                : s.detalles.stream().map(d -> d == null ? null : new Linea(d.codTelefono, d.cantidad)).toList();
        Factura factura = servicioFacturacion.facturar(sesion(), s.cedula, s.formaPago, s.numCuotas, lineas);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(factura.numero())).build())
                .entity(mapeador.deFactura(factura, true))
                .build();
    }

    @GET
    @Path("{numFactura: \\d+}")
    public FacturaDto buscar(@PathParam("numFactura") int numFactura) throws ExcepcionServicio {
        return mapeador.deFactura(servicioFacturacion.buscar(numFactura), true);
    }

    /**
     * Sin cédula lista las últimas facturas de todos los clientes.
     */
    @GET
    public List<FacturaDto> listar(@QueryParam("cedula") String cedula) throws ExcepcionServicio {
        return mapeador.deFacturas(servicioFacturacion.listar(cedula));
    }

    private Sesion sesion() {
        return (Sesion) peticion.getAttribute(ManejadorAutenticacion.PROPIEDAD_SESION);
    }
}
