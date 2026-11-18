using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;

namespace ec.edu.monster.modelo.dao
{
    /// <summary>
    /// Comprueba que la migración InitialCreate (carpeta Migrations) corresponde al modelo de
    /// AppDbContext: si alguien cambia una entidad sin ejecutar Add-Migration, esta prueba falla.
    /// No se conecta a SQL Server.
    /// </summary>
    public class MigracionesTest
    {
        [Fact]
        public void NoHayCambiosDelModeloSinMigracion()
        {
            DbContextOptions<AppDbContext> opciones = new DbContextOptionsBuilder<AppDbContext>()
                .UseSqlServer("Server=localhost;Database=ApiPaisesDB;Trusted_Connection=True;")
                .Options;
            using AppDbContext contexto = new AppDbContext(opciones);

#pragma warning disable EF1001 // API interna de EF Core: es la misma comparación que hace Add-Migration
            IModel? instantanea = contexto.GetService<IMigrationsAssembly>().ModelSnapshot?.Model;
            if (instantanea is IMutableModel mutable)
            {
                instantanea = mutable.FinalizeModel();
            }
            Assert.NotNull(instantanea);
            instantanea = contexto.GetService<IModelRuntimeInitializer>().Initialize(instantanea);

            bool hayDiferencias = contexto.GetService<IMigrationsModelDiffer>().HasDifferences(
                instantanea.GetRelationalModel(),
                contexto.GetService<IDesignTimeModel>().Model.GetRelationalModel());
#pragma warning restore EF1001

            Assert.False(hayDiferencias, "El modelo cambió: ejecute Add-Migration.");
        }
    }
}
