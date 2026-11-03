using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster;

/// <summary>Ventana única de la aplicación; las pantallas las dibuja la vista Blazor de EurekaBank.Shared.</summary>
public partial class App : Application
{
    private readonly EstadoSesion estado;
    private readonly AutenticacionSoapService autenticacion;

    public App(EstadoSesion estado, AutenticacionSoapService autenticacion)
    {
        InitializeComponent();
        this.estado = estado;
        this.autenticacion = autenticacion;
    }

    protected override Window CreateWindow(IActivationState? activationState)
    {
        Window ventana = new(new MainPage())
        {
            Title = "EurekaBank | Cliente de escritorio SOAP",
            Width = 1280,
            Height = 820,
            MinimumWidth = 420,
            MinimumHeight = 600
        };
        ventana.Destroying += (_, _) => CerrarSesionAlSalir();
        return ventana;
    }

    /// <summary>
    /// Al cerrar la ventana con la sesión abierta también se invalida el token (RQF-002). Se
    /// espera como máximo unos segundos para no retener el cierre si el servidor no responde.
    /// </summary>
    private void CerrarSesionAlSalir()
    {
        SesionModelo? sesion = estado.Sesion;
        if (sesion == null)
        {
            return;
        }
        estado.Terminar(null, false);
        try
        {
            Task.Run(() => autenticacion.CerrarSesionAsync(sesion.Token)).Wait(TimeSpan.FromSeconds(3));
        }
        catch (AggregateException)
        {
            // La sesión local se descarta de todas formas
        }
    }
}
