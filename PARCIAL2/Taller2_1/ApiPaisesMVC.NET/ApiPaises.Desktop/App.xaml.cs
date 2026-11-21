namespace ec.edu.monster;

/// <summary>Ventana única de la aplicación; las pantallas las dibuja la vista Blazor de ApiPaises.Shared.</summary>
public partial class App : Application
{
    public App()
    {
        InitializeComponent();
    }

    protected override Window CreateWindow(IActivationState? activationState)
    {
        return new Window(new MainPage())
        {
            Title = "Países API | Taller 2.1 · Grupo 03",
            Width = 1366,
            Height = 860,
            // Debajo de este tamaño la tabla y el formulario dejarían de caber lado a lado
            MinimumWidth = 1120,
            MinimumHeight = 720
        };
    }
}
