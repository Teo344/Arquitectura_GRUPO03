using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.servicio
{
    public class PaisServicioTest
    {
        private static Pais Ecuador() => Pais.Nuevo("ECU", "Ecuador", "Quito", Region.AMERICAS, 18_000_000, "Dólar", "South America", 276_841);
        private static Pais Peru() => Pais.Nuevo("PER", "Perú", "Lima", Region.AMERICAS, 34_000_000, "Sol", "South America", 1_285_216);
        private static Pais Espana() => Pais.Nuevo("ESP", "España", "Madrid", Region.EUROPA, 48_000_000, "Euro", "Southern Europe", 505_992);

        /// <summary>API falsa: devuelve la lista configurada o falla si se indica.</summary>
        private sealed class ApiFalsa : IPaisApiCliente
        {
            public List<Pais> Paises { get; set; } = new List<Pais>();
            public bool Caida { get; set; }

            public Task<IReadOnlyList<Pais>> ObtenerTodosAsync()
            {
                FallarSiEstaCaida();
                return Task.FromResult<IReadOnlyList<Pais>>(Paises);
            }

            public Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string nombre)
            {
                FallarSiEstaCaida();
                return Task.FromResult<IReadOnlyList<Pais>>(Paises
                    .Where(p => p.Nombre!.Contains(nombre, StringComparison.OrdinalIgnoreCase)).ToList());
            }

            private void FallarSiEstaCaida()
            {
                if (Caida)
                {
                    throw new ExcepcionApiExterna("La API REST Countries no respondió a tiempo.");
                }
            }
        }

        /// <summary>DAO cuya base de datos no responde.</summary>
        private sealed class DaoCaido : PaisDAOEnMemoria
        {
            public override Task<long> ContarAsync()
            {
                throw new ExcepcionPersistencia("No se pudo conectar a la base de datos.", null);
            }
        }

        private readonly PaisDAOEnMemoria dao = new PaisDAOEnMemoria();
        private readonly ApiFalsa api = new ApiFalsa();
        private readonly PaisServicio servicio;

        public PaisServicioTest()
        {
            servicio = new PaisServicio(dao, api);
        }

        // ---------------------------------------------------------- CU-01

        [Fact]
        public async Task ReimportarActualizaEnLugarDeDuplicar()
        {
            api.Paises = new List<Pais> { Ecuador(), Peru() };
            ResultadoImportacion primera = await servicio.ImportarDesdeApiAsync();

            Pais ecuadorActualizado = Ecuador();
            ecuadorActualizado.Poblacion = 18_500_000;
            api.Paises = new List<Pais> { ecuadorActualizado, Peru(), Espana() };
            ResultadoImportacion segunda = await servicio.ImportarDesdeApiAsync();

            Assert.Equal(new ResultadoImportacion(2, 2, 0), primera);
            Assert.Equal(new ResultadoImportacion(3, 1, 2), segunda);
            Assert.Equal(3, dao.Cantidad);
            Assert.Equal(18_500_000, dao.PorCodigo()["ECU"].Poblacion);
        }

        [Fact]
        public async Task ImportarOmiteInvalidosYCodigosRepetidos()
        {
            Pais sinNombre = Pais.Nuevo("XXA", " ", null, Region.ASIA, 10, null);
            api.Paises = new List<Pais> { Ecuador(), sinNombre, Ecuador() };

            ResultadoImportacion resultado = await servicio.ImportarDesdeApiAsync();

            Assert.Equal(1, resultado.Recibidos);
            Assert.Equal(1, dao.Cantidad);
        }

        [Fact]
        public async Task SiLaApiFallaLaImportacionNoTocaLaBase()
        {
            await dao.InsertarAsync(Ecuador());
            api.Caida = true;

            await Assert.ThrowsAsync<ExcepcionApiExterna>(() => servicio.ImportarDesdeApiAsync());
            Assert.Equal(1, dao.Cantidad);
        }

        // ---------------------------------------------------------- CU-02

        [Fact]
        public async Task BuscarPorNombreConsultaLaApi()
        {
            api.Paises = new List<Pais> { Ecuador(), Peru() };

            ResultadoBusqueda resultado = await servicio.BuscarPorNombreAsync("  ecua ");

            Assert.Equal(OrigenDatos.API_EXTERNA, resultado.Origen);
            Assert.Equal("ECU", Assert.Single(resultado.Paises).Codigo);
            Assert.Null(resultado.Aviso);
        }

        [Fact]
        public async Task SiLaApiFallaLaBusquedaRespondeConLaBaseLocal()
        {
            await dao.InsertarAsync(Ecuador());
            api.Caida = true;

            ResultadoBusqueda resultado = await servicio.BuscarPorNombreAsync("ecua");

            Assert.Equal(OrigenDatos.BASE_LOCAL, resultado.Origen);
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
            Pais registrado = await servicio.RegistrarAsync(Ecuador());

            Assert.False(registrado.EsNuevo);
            Assert.Equal("Ecuador", (await servicio.ObtenerPorIdAsync(registrado.Id)).Nombre);
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
        public async Task RegistrarRechazaAreaNegativa()
        {
            Pais invalido = Ecuador();
            invalido.Area = -5;

            ExcepcionValidacion error = await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.RegistrarAsync(invalido));

            Assert.Contains("El área no puede ser negativa.", error.Errores);
        }

        [Fact]
        public async Task RegistrarRechazaCodigoDuplicado()
        {
            await servicio.RegistrarAsync(Ecuador());

            await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.RegistrarAsync(Ecuador()));
        }

        [Fact]
        public async Task ActualizarCambiaLosDatos()
        {
            Pais registrado = await servicio.RegistrarAsync(Ecuador());
            Pais cambio = Ecuador().ConId(registrado.Id);
            cambio.Capital = "Guayaquil";

            await servicio.ActualizarAsync(cambio);

            Assert.Equal("Guayaquil", (await servicio.ObtenerPorIdAsync(registrado.Id)).Capital);
        }

        [Fact]
        public async Task ActualizarRechazaElCodigoDeOtroPais()
        {
            await servicio.RegistrarAsync(Ecuador());
            Pais peru = await servicio.RegistrarAsync(Peru());

            Pais conCodigoAjeno = Pais.Nuevo("ECU", "Perú", "Lima", Region.AMERICAS, 1, null).ConId(peru.Id);

            await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.ActualizarAsync(conCodigoAjeno));
        }

        [Fact]
        public async Task ActualizarUnIdInexistenteFalla()
        {
            await Assert.ThrowsAsync<ExcepcionNoEncontrado>(() => servicio.ActualizarAsync(Ecuador().ConId(99)));
        }

        [Fact]
        public async Task EliminarUnIdInexistenteFalla()
        {
            await Assert.ThrowsAsync<ExcepcionNoEncontrado>(() => servicio.EliminarAsync(99));
        }

        // ---------------------------------------------------------- CU-07 y Actividad 3

        [Fact]
        public async Task BuscarPorRegionFiltra()
        {
            await servicio.RegistrarAsync(Ecuador());
            await servicio.RegistrarAsync(Espana());

            IReadOnlyList<Pais> europa = await servicio.BuscarPorRegionAsync(Region.EUROPA);

            Assert.Equal(new[] { "España" }, europa.Select(p => p.Nombre));
            await Assert.ThrowsAsync<ExcepcionValidacion>(() => servicio.BuscarPorRegionAsync(null));
        }

        [Fact]
        public async Task BuscarPorCapitalYCombinarConRegion()
        {
            await servicio.RegistrarAsync(Ecuador());
            await servicio.RegistrarAsync(Peru());
            await servicio.RegistrarAsync(Espana());

            IReadOnlyList<Pais> porCapital = await servicio.BuscarAsync(new FiltroPaises(CampoBusqueda.CAPITAL, "lim", null));
            IReadOnlyList<Pais> sinCoincidir = await servicio.BuscarAsync(new FiltroPaises(CampoBusqueda.CAPITAL, "lim", Region.EUROPA));
            IReadOnlyList<Pais> todos = await servicio.BuscarAsync(FiltroPaises.Ninguno);

            Assert.Equal("PER", Assert.Single(porCapital).Codigo);
            Assert.Empty(sinCoincidir);
            Assert.Equal(3, todos.Count);
        }

        [Fact]
        public async Task ConteoPorRegionIncluyeLasRegionesVacias()
        {
            await servicio.RegistrarAsync(Ecuador());

            IReadOnlyList<KeyValuePair<Region, long>> conteo = await servicio.ContarPorRegionAsync();

            Assert.Equal(RegionExtensiones.Todas.Count, conteo.Count);
            Assert.Equal(1, conteo.Single(c => c.Key == Region.AMERICAS).Value);
            Assert.Equal(0, conteo.Single(c => c.Key == Region.ASIA).Value);
        }

        // ---------------------------------------------------------- CU-08 (Actividad 5)

        [Fact]
        public async Task GeneraEstadisticas()
        {
            await servicio.RegistrarAsync(Espana());
            await servicio.RegistrarAsync(Ecuador());
            await servicio.RegistrarAsync(Peru());

            EstadisticasPaises estadisticas = await servicio.GenerarEstadisticasAsync();

            Assert.Equal(3, estadisticas.TotalPaises);
            Assert.Equal(new[] { Region.AMERICAS, Region.EUROPA }, estadisticas.PaisesPorRegion.Select(e => e.Key));
            Assert.Equal(2L, estadisticas.PaisesPorRegion[0].Value);
            Assert.Equal(Region.AMERICAS, estadisticas.RegionConMasPaises);
            Assert.Equal("ESP", estadisticas.PaisMasPoblado!.Codigo);
            // Reporte 3: Europa 48 M; América (18 M + 34 M) / 2 = 26 M
            Assert.Equal(new[] { Region.EUROPA, Region.AMERICAS }, estadisticas.PromedioPorRegion.Select(e => e.Key));
            Assert.Equal(26_000_000d, estadisticas.PromedioPorRegion[1].Value);
        }

        [Fact]
        public async Task EstadoInformaLaBaseDisponible()
        {
            await servicio.RegistrarAsync(Ecuador());

            EstadoSistema estado = await servicio.VerificarEstadoAsync();

            Assert.True(estado.BaseDisponible);
            Assert.Equal(1, estado.TotalPaises);
            Assert.Null(estado.DetalleBase);
        }

        [Fact]
        public async Task EstadoInformaLaBaseCaidaSinLanzarExcepcion()
        {
            EstadoSistema estado = await new PaisServicio(new DaoCaido(), api).VerificarEstadoAsync();

            Assert.False(estado.BaseDisponible);
            Assert.Equal("No se pudo conectar a la base de datos.", estado.DetalleBase);
        }

        [Fact]
        public async Task EstadisticasSinPaises()
        {
            EstadisticasPaises estadisticas = await servicio.GenerarEstadisticasAsync();

            Assert.True(estadisticas.EstaVacio);
            Assert.Null(estadisticas.PaisMasPoblado);
            Assert.Null(estadisticas.RegionConMasPaises);
        }
    }
}
