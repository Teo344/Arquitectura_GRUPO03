-- =============================================================================
-- Consultas de la Actividad 5 (reportes), para ejecutarlas en SSMS.
-- La aplicación obtiene los mismos resultados con LINQ en PaisDAOSqlServer
-- (Entity Framework traduce cada consulta a este SQL, parametrizado).
-- =============================================================================
USE ApiPaisesDB;
GO

-- Reporte 1: cantidad de países por región
SELECT Region,
       COUNT(*) Total
FROM Paises
GROUP BY Region
ORDER BY Total DESC;

-- Reporte 2: país más poblado
SELECT TOP 1 *
FROM Paises
ORDER BY Poblacion DESC;

-- Reporte 3: promedio poblacional por región
SELECT Region,
       AVG(CAST(Poblacion AS float)) Promedio
FROM Paises
GROUP BY Region
ORDER BY Promedio DESC;

-- Actividad avanzada: universidades por país
SELECT Pais,
       COUNT(*) Total
FROM Universidades
GROUP BY Pais
ORDER BY Pais;
