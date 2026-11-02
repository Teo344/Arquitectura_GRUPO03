using System.Text.Json.Serialization;
using ec.edu.monster.controladores.recursos;
using ec.edu.monster.datos;
using ec.edu.monster.modelo;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;

var builder = WebApplication.CreateBuilder(args);

// Conexión a SQL Server (RNF-010): copiar conexion.json.example como conexion.json
builder.Configuration.AddJsonFile("conexion.json", optional: true, reloadOnChange: false);
ConexionBD.Instancia.Configurar(builder.Configuration.GetConnectionString("eurekabank"));

// Controladores con JSON en camelCase, sin campos nulos y con números estrictos ("5" no es número)
builder.Services
    .AddControllers(opciones => opciones.Filters.Add<ManejadorErrores>())
    .AddJsonOptions(opciones =>
    {
        opciones.JsonSerializerOptions.DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull;
        opciones.JsonSerializerOptions.NumberHandling = JsonNumberHandling.Strict;
    })
    .ConfigureApiBehaviorOptions(opciones =>
    {
        // JSON mal formado o un valor que no es número en "importe": DATA-001
        opciones.InvalidModelStateResponseFactory = _ =>
            ManejadorErrores.Respuesta(new ExcepcionServicio(CodigoError.Data001));
        // Sin ProblemDetails: los 404/405/415 los arma ManejadorErrores.EscribirEstadoHttp
        opciones.SuppressMapClientErrors = true;
    });

// Una sola instancia de cada servicio para toda la aplicación (como @ApplicationScoped en Java)
builder.Services.AddSingleton<GestorToken>();
builder.Services.AddSingleton<ServicioAutenticacion>();
builder.Services.AddSingleton<ServicioCuenta>();
builder.Services.AddSingleton<ServicioTransaccion>();

// Páginas web de los orígenes configurados pueden llamar al servicio desde el navegador
string[] origenes = builder.Configuration.GetSection("Cors:Origenes").Get<string[]>() ?? Array.Empty<string>();
builder.Services.AddCors(opciones => opciones.AddDefaultPolicy(politica => politica
    .WithOrigins(origenes)
    .WithMethods("GET", "POST")
    .WithHeaders("Content-Type", "Authorization")
    .SetPreflightMaxAge(TimeSpan.FromHours(1))));

var app = builder.Build();

app.UseStatusCodePages(contexto => ManejadorErrores.EscribirEstadoHttp(contexto.HttpContext));
app.UseCors();
app.MapControllers();

app.Run();
