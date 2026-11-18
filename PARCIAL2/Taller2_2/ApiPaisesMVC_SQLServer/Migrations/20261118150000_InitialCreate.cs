using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ec.edu.monster.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "Paises",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Codigo = table.Column<string>(type: "nchar(3)", fixedLength: true, maxLength: 3, nullable: false),
                    Nombre = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false),
                    Capital = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: true),
                    Region = table.Column<string>(type: "nvarchar(20)", maxLength: 20, nullable: false),
                    SubRegion = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: true),
                    Poblacion = table.Column<long>(type: "bigint", nullable: false),
                    Area = table.Column<double>(type: "float", nullable: false),
                    Moneda = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Paises", x => x.Id);
                    table.CheckConstraint("CK_Paises_Area", "[Area] >= 0");
                    table.CheckConstraint("CK_Paises_Poblacion", "[Poblacion] >= 0");
                });

            migrationBuilder.CreateTable(
                name: "Universidades",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Nombre = table.Column<string>(type: "nvarchar(200)", maxLength: 200, nullable: false),
                    Dominio = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: true),
                    SitioWeb = table.Column<string>(type: "nvarchar(200)", maxLength: 200, nullable: true),
                    Pais = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Universidades", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "IX_Paises_Codigo",
                table: "Paises",
                column: "Codigo",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_Paises_Nombre",
                table: "Paises",
                column: "Nombre");

            migrationBuilder.CreateIndex(
                name: "IX_Paises_Region",
                table: "Paises",
                column: "Region");

            migrationBuilder.CreateIndex(
                name: "IX_Universidades_Pais_Nombre",
                table: "Universidades",
                columns: new[] { "Pais", "Nombre" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "Paises");

            migrationBuilder.DropTable(
                name: "Universidades");
        }
    }
}
