package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Cuota;
import ec.edu.monster.servicios.CalculadoraAmortizacion;
import java.math.BigDecimal;
import java.util.List;

/**
 * Prueba unitaria de la tabla de amortización: no necesita servidor ni base
 * de datos. El caso base es el ejemplo del enunciado (450 a 6 cuotas, 16.5 %
 * anual, cuota 78.65).
 *
 * @author carlo
 */
public class PruebaCalculoAmortizacion {

    private static final BigDecimal TASA = new BigDecimal("16.50");
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaCalculoAmortizacion ===");
        comprobar("la tasa mensual de 16.50 % es 0.01375",
                CalculadoraAmortizacion.tasaMensual(TASA).compareTo(new BigDecimal("0.01375")) == 0);
        comprobar("450 a 6 cuotas da una cuota de 78.65",
                CalculadoraAmortizacion.valorCuota(new BigDecimal("450.00"), 6, TASA).equals(new BigDecimal("78.65")));

        // numero, interes, capital, saldo de la tabla del enunciado; la cuota 6 cierra en 0.00
        String[][] esperada = {
            {"1", "6.19", "72.46", "377.54"},
            {"2", "5.19", "73.46", "304.08"},
            {"3", "4.18", "74.47", "229.61"},
            {"4", "3.16", "75.49", "154.12"},
            {"5", "2.12", "76.53", "77.59"},
            {"6", "1.06", "77.59", "0.00"}};
        List<Cuota> tabla = CalculadoraAmortizacion.tabla(new BigDecimal("450.00"), 6, TASA);
        comprobar("la tabla tiene 6 cuotas", tabla.size() == 6);
        for (int i = 0; i < esperada.length && i < tabla.size(); i++) {
            Cuota c = tabla.get(i);
            comprobar("cuota " + esperada[i][0] + " = 78.65 / " + esperada[i][1] + " / " + esperada[i][2] + " / "
                    + esperada[i][3], c.numero() == Integer.parseInt(esperada[i][0])
                    && c.valorCuota().equals(new BigDecimal("78.65"))
                    && c.interes().equals(new BigDecimal(esperada[i][1]))
                    && c.capital().equals(new BigDecimal(esperada[i][2]))
                    && c.saldo().equals(new BigDecimal(esperada[i][3])));
        }

        boolean cuadran = true;
        for (String monto : new String[]{"199.00", "450.00", "945.00", "2250.00", "2399.00"}) {
            for (int n = 3; n <= 18; n++) {
                cuadran &= cuadra(new BigDecimal(monto), n);
            }
        }
        comprobar("de 3 a 18 cuotas: el capital suma el monto, la cuota es fija y el saldo cierra en 0.00", cuadran);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static boolean cuadra(BigDecimal monto, int numCuotas) {
        List<Cuota> tabla = CalculadoraAmortizacion.tabla(monto, numCuotas, TASA);
        BigDecimal capital = BigDecimal.ZERO;
        boolean correcto = tabla.size() == numCuotas;
        for (Cuota c : tabla) {
            capital = capital.add(c.capital());
            correcto &= c.valorCuota().equals(tabla.get(0).valorCuota())
                    && c.interes().signum() >= 0 && c.capital().signum() > 0
                    && c.valorCuota().equals(c.interes().add(c.capital()));
        }
        correcto &= capital.equals(monto) && tabla.get(numCuotas - 1).saldo().signum() == 0;
        if (!correcto) {
            System.out.println("        no cuadra: " + monto + " a " + numCuotas + " cuotas");
        }
        return correcto;
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
