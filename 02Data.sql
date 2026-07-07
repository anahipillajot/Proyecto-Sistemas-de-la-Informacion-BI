USE ProyectoBI_GenZ;
GO

-- 1. Llenar Dim_Plataformas con los valores únicos de Col5
INSERT INTO Dim_Plataformas (NombrePlataforma)
SELECT DISTINCT Col5 
FROM Tmp_Kaggle_Raw 
WHERE Col5 IS NOT NULL AND Col5 <> '';

-- 2. Llenar Dim_Paises con los valores únicos de Col3
INSERT INTO Dim_Paises (NombrePais)
SELECT DISTINCT Col3 
FROM Tmp_Kaggle_Raw 
WHERE Col3 IS NOT NULL AND Col3 <> '';

-- 3. Llenar Dim_Demografia con las combinaciones de Edad (Col1) y Género (Col2)
INSERT INTO Dim_Demografia (Edad, Genero)
SELECT DISTINCT CAST(Col1 AS INT), Col2 
FROM Tmp_Kaggle_Raw 
WHERE Col1 IS NOT NULL AND Col2 IS NOT NULL;
GO

-- 4. LLENAR LA TABLA DE HECHOS RELACIONANDO TODO EL MILLÓN DE FILAS
-- Usamos SUBSTRING para extraer de forma segura el número de bienestar de Col6
INSERT INTO Fact_Uso_Redes (ID_Demografia, ID_Plataforma, ID_Pais, TiempoPantalla, BienestarMental)
SELECT 
    d.ID_Demografia,
    p.ID_Plataforma,
    c.ID_Pais,
    CAST(raw.Col4 AS DECIMAL(10,2)),
    CAST(SUBSTRING(raw.Col6, 1, CHARINDEX(',', raw.Col6) - 1) AS INT) -- Extrae el score antes de la primera coma
FROM Tmp_Kaggle_Raw raw
JOIN Dim_Demografia d ON CAST(raw.Col1 AS INT) = d.Edad AND raw.Col2 = d.Genero
JOIN Dim_Plataformas p ON raw.Col5 = p.NombrePlataforma
JOIN Dim_Paises c ON raw.Col3 = c.NombrePais
WHERE CHARINDEX(',', raw.Col6) > 0; -- Asegura que la columna tiene el formato esperado
GO

-- 5. Verificar que tu tabla relacional final tenga el millón de datos
SELECT COUNT(*) AS Registros_En_Modelo_Estrella FROM Fact_Uso_Redes;
GO

-- 6. Limpieza final: Eliminamos la tabla temporal para liberar espacio
DROP TABLE Tmp_Kaggle_Raw;
GO

USE ProyectoBI_GenZ;
GO

-- Insertar un usuario Administrador (Rol 1)
INSERT INTO Usuarios (Username, PasswordHash, ID_Rol) 
VALUES ('admin', 'admin123', 1);

-- Insertar un usuario Analista (Rol 2)
INSERT INTO Usuarios (Username, PasswordHash, ID_Rol) 
VALUES ('analista', 'analista123', 2);
GO