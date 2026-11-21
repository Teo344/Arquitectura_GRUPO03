using ec.edu.monster.controlador;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Xunit;

namespace ec.edu.monster.pruebas;

/// <summary>Prueba la conversión del formulario, que no necesita al servicio.</summary>
public class PaisControladorTest
{
    [Fact]
    public void ConvierteElFormularioEnPais()
    {
        FormularioPais formulario = new(" ecu ", " Ecuador ", "Quito", "america", "18.001.000", "");

        Pais pais = PaisControlador.Convertir(7, formulario);

        Assert.Equal(new Pais(7, "ECU", "Ecuador", "Quito", Region.Americas, 18_001_000, null), pais);
    }

    [Theory]
    [InlineData("Oceanía", Region.Oceania)]
    [InlineData("oceania", Region.Oceania)]
    [InlineData("Europe", Region.Europa)]
    [InlineData("ÁFRICA", Region.Africa)]
    public void AceptaRegionesEnEspanolConOSinTildeYEnIngles(string texto, Region esperada)
    {
        Assert.Equal(esperada, Regiones.DesdeTexto(texto));
    }

    [Fact]
    public void ReuneLosErroresDeRegionYPoblacion()
    {
        FormularioPais formulario = new("ECU", "Ecuador", null, "Atlántida", "muchos", null);

        ExcepcionValidacion error = Assert.Throws<ExcepcionValidacion>(() => PaisControlador.Convertir(null, formulario));

        Assert.Equal(2, error.Errores.Count);
    }

    [Theory]
    [InlineData("18,001,000", 18_001_000)]
    [InlineData("18 001 000", 18_001_000)]
    [InlineData("18.001.000", 18_001_000)]
    public void PoblacionAceptaSeparadoresDeMiles(string texto, long esperada)
    {
        Assert.Equal(esperada, PaisControlador.ConvertirPoblacion(texto));
    }

    [Theory]
    [InlineData("")]
    [InlineData("12abc")]
    public void PoblacionInvalidaSeRechaza(string texto)
    {
        Assert.Throws<ExcepcionValidacion>(() => PaisControlador.ConvertirPoblacion(texto));
    }

    [Fact]
    public void CapitalVaciaQuedaNula()
    {
        Assert.Null(PaisControlador.Convertir(null, new FormularioPais("ECU", "Ecuador", "  ", "Asia", "1", null)).Capital);
    }
}
