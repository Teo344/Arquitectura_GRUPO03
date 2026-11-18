using ec.edu.monster.modelo.entidad;
using Microsoft.EntityFrameworkCore;

namespace ec.edu.monster.modelo.dao
{
    /// <summary>
    /// Contexto de Entity Framework Core para la base ApiPaisesDB de SQL Server. Las migraciones
    /// (Add-Migration InitialCreate / Update-Database) crean las tablas Paises y Universidades a
    /// partir de esta configuración.
    /// </summary>
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options)
            : base(options)
        {
        }

        public DbSet<Pais> Paises { get; set; } = null!;

        public DbSet<Universidad> Universidades { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Pais>(pais =>
            {
                pais.ToTable("Paises");
                pais.HasKey(p => p.Id);
                // Código ISO alfa-3 único: reimportar actualiza en vez de duplicar
                pais.Property(p => p.Codigo).HasMaxLength(3).IsFixedLength().IsRequired();
                pais.HasIndex(p => p.Codigo).IsUnique();
                pais.Property(p => p.Nombre).HasMaxLength(100).IsRequired();
                pais.HasIndex(p => p.Nombre);
                pais.Property(p => p.Capital).HasMaxLength(100);
                // Se guarda el nombre de la constante (AMERICAS, EUROPA...) y no un número
                pais.Property(p => p.Region).HasConversion<string>().HasMaxLength(20).IsRequired();
                pais.HasIndex(p => p.Region);
                pais.Property(p => p.SubRegion).HasMaxLength(100);
                pais.Property(p => p.Moneda).HasMaxLength(100);
                pais.ToTable(t =>
                {
                    t.HasCheckConstraint("CK_Paises_Poblacion", "[Poblacion] >= 0");
                    t.HasCheckConstraint("CK_Paises_Area", "[Area] >= 0");
                });
                pais.Ignore(p => p.EsNuevo);
            });

            modelBuilder.Entity<Universidad>(universidad =>
            {
                universidad.ToTable("Universidades");
                universidad.HasKey(u => u.Id);
                universidad.Property(u => u.Nombre).HasMaxLength(200).IsRequired();
                universidad.Property(u => u.Dominio).HasMaxLength(100);
                universidad.Property(u => u.SitioWeb).HasMaxLength(200);
                universidad.Property(u => u.Pais).HasMaxLength(100).IsRequired();
                // Nombre + país único: reimportar no duplica
                universidad.HasIndex(u => new { u.Pais, u.Nombre }).IsUnique();
            });
        }
    }
}
