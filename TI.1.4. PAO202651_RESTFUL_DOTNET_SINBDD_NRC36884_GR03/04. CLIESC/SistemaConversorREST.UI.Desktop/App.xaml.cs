namespace SistemaConversorREST.UI.Desktop
{
    public partial class App : Application
    {
        public App()
        {
            InitializeComponent();
        }

        protected override Window CreateWindow(IActivationState? activationState)
        {
            return new Window(new MainPage()) { Title = "SistemaConversorREST.UI.Desktop" };
        }
    }
}
