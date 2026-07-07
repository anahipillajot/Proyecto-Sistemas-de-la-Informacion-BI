USE ProyectoBI_GenZ;
GO

-- 1. Eliminamos la tabla temporal anterior si existe para recrearla limpia
IF OBJECT_ID('Tmp_Kaggle_Raw', 'U') IS NOT NULL
    DROP TABLE Tmp_Kaggle_Raw;
GO

-- 2. Creamos la tabla temporal donde TODO es texto (así no hay errores de conversión)
CREATE TABLE Tmp_Kaggle_Raw (
    Col1 VARCHAR(250),
    Col2 VARCHAR(250),
    Col3 VARCHAR(250),
    Col4 VARCHAR(250),
    Col5 VARCHAR(250),
    Col6 VARCHAR(250)
);
GO

-- 3. Carga masiva (Ahora va a entrar limpio porque el texto acepta cualquier cosa)
BULK INSERT Tmp_Kaggle_Raw
FROM 'C:\Data\genz_social_media_usage_1M.csv'
WITH (
    FIELDTERMINATOR = ',',     
    ROWTERMINATOR = '0x0a',    
    FIRSTROW = 2,              
    CODEPAGE = '65001'         
);
GO

-- 4. Verificar cuántas filas entraron
SELECT COUNT(*) AS Datos_Cargados_Temporalmente FROM Tmp_Kaggle_Raw;
GO

-- 5. Opcional: Ver las primeras 5 filas para comprobar qué hay en cada columna
SELECT TOP 5 * FROM Tmp_Kaggle_Raw;
GO


