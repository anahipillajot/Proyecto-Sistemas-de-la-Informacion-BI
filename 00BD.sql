-- 1. CREAR LA BASE DE DATOS (Ejecuta esto primero solo)
CREATE DATABASE ProyectoBI_GenZ;
GO

USE ProyectoBI_GenZ;
GO

drop table if exists Fact_Uso_Redes;
drop table if exists Dim_Plataformas;
drop table if exists Dim_Paises;
drop table if exists Dim_Demografia;
drop table if exists Usuarios;
drop table if exists Roles;



CREATE TABLE Roles (
    ID_Rol INT PRIMARY KEY IDENTITY(1,1),
    NombreRol VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Usuarios (
    ID_Usuario INT PRIMARY KEY IDENTITY(1,1),
    Username VARCHAR(50) NOT NULL UNIQUE,
    PasswordHash CHAR(64) NOT NULL, -- SHA-256
    ID_Rol INT FOREIGN KEY REFERENCES Roles(ID_Rol)
);

-- Insertar los roles requeridos
INSERT INTO Roles (NombreRol) VALUES ('Administrador'), ('Analista');
GO



-- Dimensión 1: Plataformas de Redes Sociales
CREATE TABLE Dim_Plataformas (
    ID_Plataforma INT PRIMARY KEY IDENTITY(1,1),
    NombrePlataforma VARCHAR(50) NOT NULL UNIQUE
);

-- Dimensión 2: Países / Ubicación Geográfica
CREATE TABLE Dim_Paises (
    ID_Pais INT PRIMARY KEY IDENTITY(1,1),
    NombrePais VARCHAR(100) NOT NULL UNIQUE
);

-- Dimensión 3: Demografía (Agrupación de edad y género)
CREATE TABLE Dim_Demografia (
    ID_Demografia INT PRIMARY KEY IDENTITY(1,1),
    Edad INT NOT NULL,
    Genero VARCHAR(30) NOT NULL
);

-- TABLA CENTRAL: Hechos de Uso de Redes Sociales (El Millón de Filas)
CREATE TABLE Fact_Uso_Redes (
    ID_Hecho INT PRIMARY KEY IDENTITY(1,1),
    ID_Demografia INT FOREIGN KEY REFERENCES Dim_Demografia(ID_Demografia),
    ID_Plataforma INT FOREIGN KEY REFERENCES Dim_Plataformas(ID_Plataforma),
    ID_Pais INT FOREIGN KEY REFERENCES Dim_Paises(ID_Pais),
    TiempoPantalla DECIMAL(5,2) NOT NULL, -- Métrica cuantitativa
    BienestarMental INT NOT NULL          -- Métrica cuantitativa
);
GO

-- Índices no agrupados para acelerar las consultas de los gráficos en Java
CREATE INDEX IX_Fact_Plataforma ON Fact_Uso_Redes(ID_Plataforma);
CREATE INDEX IX_Fact_Pais ON Fact_Uso_Redes(ID_Pais);

