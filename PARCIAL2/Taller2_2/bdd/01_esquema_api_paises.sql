-- =============================================================================
-- Esquema de la base de datos ApiPaisesDB (SQL Server 2022 o superior).
--
-- Es el mismo SQL que genera la migración InitialCreate de Entity Framework Core
-- (ApiPaisesMVC_SQLServer/Migrations) y registra la migración en __EFMigrationsHistory.
-- Por eso se puede usar cualquiera de los dos caminos, o los dos, sin conflicto:
--   * Visual Studio > Package Manager Console:  Update-Database
--   * SSMS o consola:  sqlcmd -S localhost -E -i 01_esquema_api_paises.sql
-- El script es idempotente: ejecutarlo otra vez no cambia nada.
--
-- Diferencias con el script del enunciado (sección 7):
--   * Codigo (ISO alfa-3, p. ej. ECU) es UNIQUE: reimportar actualiza en vez de duplicar.
--   * Campos obligatorios NOT NULL, población y área no negativas.
--   * Región guardada como texto fijo (AFRICA, AMERICAS, ANTARTIDA, ASIA, EUROPA, OCEANIA).
--   * Índices para la búsqueda por región y por nombre.
--   * Tabla Universidades de la actividad avanzada (nombre + país únicos).
-- =============================================================================
IF DB_ID(N'ApiPaisesDB') IS NULL
    CREATE DATABASE ApiPaisesDB;
GO

USE ApiPaisesDB;
GO

IF OBJECT_ID(N'[__EFMigrationsHistory]') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END;
GO

BEGIN TRANSACTION;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    CREATE TABLE [Paises] (
        [Id] int NOT NULL IDENTITY,
        [Codigo] nchar(3) NOT NULL,
        [Nombre] nvarchar(100) NOT NULL,
        [Capital] nvarchar(100) NULL,
        [Region] nvarchar(20) NOT NULL,
        [SubRegion] nvarchar(100) NULL,
        [Poblacion] bigint NOT NULL,
        [Area] float NOT NULL,
        [Moneda] nvarchar(100) NULL,
        CONSTRAINT [PK_Paises] PRIMARY KEY ([Id]),
        CONSTRAINT [CK_Paises_Area] CHECK ([Area] >= 0),
        CONSTRAINT [CK_Paises_Poblacion] CHECK ([Poblacion] >= 0)
    );
END;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    CREATE TABLE [Universidades] (
        [Id] int NOT NULL IDENTITY,
        [Nombre] nvarchar(200) NOT NULL,
        [Dominio] nvarchar(100) NULL,
        [SitioWeb] nvarchar(200) NULL,
        [Pais] nvarchar(100) NOT NULL,
        CONSTRAINT [PK_Universidades] PRIMARY KEY ([Id])
    );
END;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    CREATE UNIQUE INDEX [IX_Paises_Codigo] ON [Paises] ([Codigo]);
END;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    CREATE INDEX [IX_Paises_Nombre] ON [Paises] ([Nombre]);
END;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    CREATE INDEX [IX_Paises_Region] ON [Paises] ([Region]);
END;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    CREATE UNIQUE INDEX [IX_Universidades_Pais_Nombre] ON [Universidades] ([Pais], [Nombre]);
END;
GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261118150000_InitialCreate')
BEGIN
    INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
    VALUES (N'20261118150000_InitialCreate', N'8.0.34');
END;
GO

COMMIT;
GO
