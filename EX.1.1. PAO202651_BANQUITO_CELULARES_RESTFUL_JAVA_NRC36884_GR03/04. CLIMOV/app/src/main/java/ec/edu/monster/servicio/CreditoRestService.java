package ec.edu.monster.servicio;

import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Acceso a los recursos REST del crédito BanQuito (/clientes y /creditos): CU-07, CU-08 y CU-12. */
public class CreditoRestService {

    private final ClienteRest cliente;

    public CreditoRestService() {
        this(new ClienteRest());
    }

    public CreditoRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    /** Servicios 1 y 2 del banco: si es sujeto de crédito y su monto máximo. */
    public EvaluacionClienteModelo evaluarCliente(String token, String cedula) {
        JSONObject datos = cliente.get("/clientes/sujeto-credito?cedula=" + ClienteRest.codificar(cedula), token);
        List<EvaluacionClienteModelo.Motivo> motivos = new ArrayList<>();
        JSONArray lista = datos.optJSONArray("motivos");
        if (lista != null) {
            for (int i = 0; i < lista.length(); i++) {
                JSONObject motivo = lista.optJSONObject(i);
                if (motivo != null) {
                    motivos.add(new EvaluacionClienteModelo.Motivo(ClienteRest.texto(motivo, "codigo"),
                            ClienteRest.texto(motivo, "descripcion")));
                }
            }
        }
        return new EvaluacionClienteModelo(ClienteRest.texto(datos, "cedula"),
                ClienteRest.enteroOpcional(datos, "codCliente"), ClienteRest.texto(datos, "nombre"),
                datos.optBoolean("sujetoCredito"), motivos, ClienteRest.decimal(datos, "montoMaximo"));
    }

    public List<CreditoModelo> creditos(String token, String cedula) {
        JSONArray datos = cliente.getLista("/clientes/creditos?cedula=" + ClienteRest.codificar(cedula), token);
        List<CreditoModelo> creditos = new ArrayList<>(datos.length());
        for (int i = 0; i < datos.length(); i++) {
            JSONObject c = datos.optJSONObject(i);
            if (c != null) {
                creditos.add(new CreditoModelo(ClienteRest.entero(c, "codCredito"), ClienteRest.texto(c, "fecha"),
                        ClienteRest.decimal(c, "monto"), ClienteRest.entero(c, "numCuotas"),
                        ClienteRest.decimal(c, "tasaAnual"), ClienteRest.decimal(c, "valorCuota"),
                        ClienteRest.texto(c, "estado")));
            }
        }
        return Collections.unmodifiableList(creditos);
    }

    /** Servicio 4 del banco: la tabla de amortización de un crédito otorgado. */
    public List<CuotaModelo> cuotas(String token, int codCredito) {
        return aCuotas(cliente.getLista("/creditos/" + codCredito + "/cuotas", token));
    }

    /** La tabla que tendría un crédito, sin otorgarlo. */
    public List<CuotaModelo> simular(String token, BigDecimal monto, int cuotas) {
        return aCuotas(cliente.getLista("/creditos/simulacion?monto=" + ClienteRest.codificar(monto.toPlainString())
                + "&cuotas=" + cuotas, token));
    }

    private static List<CuotaModelo> aCuotas(JSONArray datos) {
        List<CuotaModelo> cuotas = new ArrayList<>(datos.length());
        for (int i = 0; i < datos.length(); i++) {
            JSONObject c = datos.optJSONObject(i);
            if (c != null) {
                cuotas.add(new CuotaModelo(ClienteRest.entero(c, "numero"), ClienteRest.decimal(c, "valorCuota"),
                        ClienteRest.decimal(c, "interes"), ClienteRest.decimal(c, "capital"),
                        ClienteRest.decimal(c, "saldo")));
            }
        }
        return Collections.unmodifiableList(cuotas);
    }
}
