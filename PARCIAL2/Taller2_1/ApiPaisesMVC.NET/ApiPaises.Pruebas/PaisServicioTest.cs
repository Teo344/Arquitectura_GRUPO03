using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using ec.edu.monster.servicio;
using Xunit;

namespace ec.edu.monster.pruebas;

public class PaisServicioTest
{
    private static readonly Pais Ecuador = Pais.Nuevo("ECU", "Ecuador", "Quito", Region.Americas, 18_000_000, "Dólar");
    private static readonly Pais Peru = Pais.Nuevo("PER", "Perú", "Lima", Region.Americas, 34_000_000, "Sol");
    private static readonly Pais Espana = Pais.Nuevo("ESP", "España", "Madrid", Region.Europa, 48_000_000, "Euro");

    private readonly PaisDAOEnMemoria dao = new();
    private readonly ApiFalsa api = new();
    private readonly PaisServicio servicio;

    public PaisServicioTest()
    {
        servicio = new PaisServicio(dao, api);
    }

    // ---------------------------------------------------------- CU-01

    [Fact]
    public async Task ReimportarActualizaEnLugarDeDuplicar()
    {
        api.Paises = [Ecuador, Peru];
        ResultadoImportacion primera = await servicio.ImportarDesdeApiAsync();

        api.Paises = [Pais.Nuevo("ECU", "Ecuador", "Quito", Region.Americas, 18_500_000, "Dólar"), Peru, Espana];
        ResultadoImportacion segunda = await servicio.ImportarDesdeApiAsync();

        Assert.Equal(new ResultadoImportacion(2, 2, 0), primera);
        Assert.Equal(new ResultadoImportacion(3, 1, 2), segunda);
        Assert.Equal(3, dao.Cantidad);
        Assert.Equal(18_500_000, dao.PorCodigo("ECU")!.Poblacion);
    }

    [Fact]
    public async Task ImportarOmiteInvalidosYCodigosRepetidos()
    {
        api.Paises = [Ecuador, Pais.Nuevo("XXA", " ", null, Region.Asia, 10, null), Ecuador];

        ResultadoImportacion resultado = await servicio.ImportarDesdeApiAsync();

        Assert.Equal(1, resultado.Recibidos);
        Assert.Equal(1, dao.Cantidad);
    }

    [Fact]
    public async Task SiLaApiFallaLaImportacionNoTocaLaBase()
    {
        await dao.InsertarAsync(Ecuador);
        api.Caida = true;

        await Assert.ThrowsAsync<ExcepcionApiExterna>(servicio.ImportarDesdeApiAsync);
        Assert.Equal(1, dao.Cantidad);
    }

    // ---------------------------------------------------------- CU-02

    [Fact]
    public async Task BuscarPorNombreConsultaLaApi()
    {
        api.Paises = [Ecuador, Peru];

        ResultadoBusqueda resultado = await servicio.BuscarPorNombreAsync("  ecua ");

        Assert.Equal(OrigenDatos.ApiExterna, resultado.Origen);
        Assert.Equal([Ecuador], resultado.Paises);
        Assert.Null(resultado.Aviso);
    }

    [Fact]
    public async Task SiLaApiFallaLaBusquedaRespondeConLaBaseLocal()
    {
        await dao.InsertarAsync(Ecuador);
        api.Caida = true;

        ResultadoBusqueda resultado = await servicio.BuscarPorNombreAsync("ecua");

        Assert.Equal(OrigenDatos.BaseLocal, resultado.Origen);
        Assert.Equal("ECU", resultado.Paises[0].Codigo);
        Assert.Contains("guardados localmente", resultado.Aviso);
    }

    [Fact]
    public async Task BusquedaVaciaSeRechazaSinLlamarALaApi()
    {
        api.Caida = true;

        await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.BuscarPorNombreAsync("  "));
    }

    // ---------------------------------------------------------- CU-03 a CU-06

    [Fact]
    public async Task RegistrarAsignaId()
    {
        Pais registrado = await servicio.RegistrarAsync(Ecuador);

        Assert.NotNull(registrado.Id);
        Assert.Equal(registrado, await servicio.ObtenerPorIdAsync(registrado.Id!.Value));
    }

    [Fact]
    public async Task RegistrarReuneTodosLosErrores()
    {
        Pais invalido = Pais.Nuevo("EC", "", null, null, -1, null);

        ExcepcionValidacion error = await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.RegistrarAsync(invalido));

        Assert.Equal(4, error.Errores.Count);
        Assert.Equal(0, dao.Cantidad);
    }

    [Fact]
    public async Task RegistrarRechazaCodigoDuplicado()
    {
        await servicio.RegistrarAsync(Ecuador);

        await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.RegistrarAsync(Ecuador));
    }

    [Fact]
    public async Task ActualizarCambiaLosDatos()
    {
        Pais registrado = await servicio.RegistrarAsync(Ecuador);

        await servicio.ActualizarAsync(new Pais(registrado.Id, "ECU", "Ecuador", "Guayaquil", Region.Americas, 18_000_000, "Dólar"));

        Assert.Equal("Guayaquil", (await servicio.ObtenerPorIdAsync(registrado.Id!.Value)).Capital);
    }

    [Fact]
    public async Task ActualizarRechazaElCodigoDeOtroPais()
    {
        await servicio.RegistrarAsync(Ecuador);
        Pais peru = await servicio.RegistrarAsync(Peru);

        Pais conCodigoAjeno = new(peru.Id, "ECU", "Perú", "Lima", Region.Americas, 1, null);

        await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.ActualizarAsync(conCodigoAjeno));
    }

    [Fact]
    public async Task ActualizarUnIdInexistenteFalla()
    {
        await Assert.ThrowsAsync<ExcepcionNoEncontrado>(() => servicio.ActualizarAsync(Ecuador.ConId(99)));
    }

    [Fact]
    public async Task EliminarUnIdInexistenteFalla()
    {
        await Assert.ThrowsAsync<ExcepcionNoEncontrado>(() => servicio.EliminarAsync(99));
    }

    // ---------------------------------------------------------- CU-07 y CU-08

    [Fact]
    public async Task BuscarPorRegionFiltra()
    {
        await servicio.RegistrarAsync(Ecuador);
        await servicio.RegistrarAsync(Espana);

        IReadOnlyList<Pais> europa = await servicio.BuscarPorRegionAsync(Region.Europa);

        Assert.Equal(["España"], europa.Select(p => p.Nombre));
        await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.BuscarPorRegionAsync(null));
    }

    [Fact]
    public async Task GeneraEstadisticas()
    {
        await servicio.RegistrarAsync(Espana);
        await servicio.RegistrarAsync(Ecuador);
        await servicio.RegistrarAsync(Peru);

        EstadisticasPaises estadisticas = await servicio.GenerarEstadisticasAsync();

        Assert.Equal(3, estadisticas.TotalPaises);
        Assert.Equal([Region.Americas, Region.Europa], estadisticas.PaisesPorRegion.Select(c => c.Region));
        Assert.Equal(2, estadisticas.PaisesPorRegion[0].Cantidad);
        Assert.Equal(Region.Americas, estadisticas.RegionConMasPaises);
        Assert.Equal("ESP", estadisticas.PaisMasPoblado!.Codigo);
    }

    [Fact]
    public async Task EstadisticasSinPaises()
    {
        EstadisticasPaises estadisticas = await servicio.GenerarEstadisticasAsync();

        Assert.True(estadisticas.EstaVacio);
        Assert.Null(estadisticas.PaisMasPoblado);
        Assert.Null(estadisticas.RegionConMasPaises);
    }

    // ---------------------------------------------------------- Inicio

    [Fact]
    public async Task EstadoInformaLaBaseDisponible()
    {
        await servicio.RegistrarAsync(Ecuador);

        EstadoSistema estado = await servicio.VerificarEstadoAsync();

        Assert.True(estado.BaseDisponible);
        Assert.Equal(1, estado.TotalPaises);
        Assert.Null(estado.DetalleBase);
    }

    [Fact]
    public async Task EstadoInformaLaBaseCaidaSinLanzarExcepcion()
    {
        EstadoSistema estado = await new PaisServicio(new PaisDAOCaido(), api).VerificarEstadoAsync();

        Assert.False(estado.BaseDisponible);
        Assert.Equal("No se pudo conectar a la base de datos.", estado.DetalleBase);
    }
}
