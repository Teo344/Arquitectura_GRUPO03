/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/WebService.java to edit this template
 */
package ec.edu.monster.ws;

import ec.edu.monster.servicios.ConvertidorServicio;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author MateoCriollo
 */
@WebService(serviceName = "WSConvertir2")
public class WSConvertir2 {


    /**
     * Web service operation
     */
    @WebMethod(operationName = "convertir")
    public int convertir(@WebParam(name = "n1") int n1, @WebParam(name = "n2") int n2) {
        ConvertidorServicio servicio = new ConvertidorServicio();
        int convertidor = servicio.convertirTiempo(n1, n2);
        return convertidor;
    }
}
