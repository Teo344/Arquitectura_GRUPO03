namespace ec.edu.monster.WinUI;

/// <summary>Punto de entrada de Windows (WinUI); crea la aplicación MAUI.</summary>
public partial class App : MauiWinUIApplication
{
    public App()
    {
        InitializeComponent();
    }

    protected override MauiApp CreateMauiApp() => MauiProgram.CreateMauiApp();
}
