using System.Text.Encodings.Web;
using System.Text.Unicode;
using ec.edu.monster.config;
using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.servicio;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.WebEncoders;

// Punto de entrada y raíz de composición (equivale a App.java del Taller 2.1): es el único lugar que
// conoce las implementaciones concretas (SQL Server, REST Countries, Hipolabs) y las registra en el
// contenedor de inyección de dependencias. El resto del código depende de interfaces.
WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

// MVC con vistas Razor; todos los POST validan el token antifalsificación
builder.Services.AddControllersWithViews(opciones =>
    opciones.Filters.Add(new AutoValidateAntiforgeryTokenAttribute()));
// Las vistas escriben tildes y eñes tal cual (sin convertirlas en entidades HTML)
builder.Services.Configure<WebEncoderOptions>(opciones =>
    opciones.TextEncoderSettings = new TextEncoderSettings(UnicodeRanges.All));

// Entity Framework Core con SQL Server (secciones 9 y 12 del enunciado)
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString(
            "SqlServerConnection")));

// Parámetros de las APIs públicas (appsettings.json)
Configuracion configuracion = Configuracion.Cargar(builder.Configuration);
builder.Services.AddSingleton(configuracion);

// Modelo: clientes de las APIs (HttpClient administrado por la fábrica) y DAOs
builder.Services.AddSingleton<RestCountriesMapper>();
builder.Services.AddSingleton<HipolabsMapper>();
builder.Services.AddHttpClient<IPaisApiCliente, RestCountriesCliente>();
builder.Services.AddHttpClient<IUniversidadApiCliente, HipolabsCliente>();
builder.Services.AddScoped<IPaisDAO, PaisDAOSqlServer>();
builder.Services.AddScoped<IUniversidadDAO, UniversidadDAOSqlServer>();

// Servicio: reglas de negocio
builder.Services.AddScoped<PaisServicio>();
builder.Services.AddScoped<UniversidadServicio>();

WebApplication app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();
app.UseAuthorization();

// Los controladores de ec.edu.monster.controlador usan rutas por atributo en español:
// /  /paises  /busqueda  /reportes  /universidades
app.MapControllers();

app.Run();
