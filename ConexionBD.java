import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Cadena de conexión adaptada para SQL Server local y tu BD
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=ProyectoBI_GenZ;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa"; 
    private static final String PASSWORD = "123"; // <-- REEMPLAZA CON TU CONTRASEÑA REAL

    public static Connection getConexion() {
        Connection conexion = null;
        try {
            // Registrar explícitamente el Driver JDBC que acabas de agregar
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa al Modelo Estrella en SQL Server!");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el Driver JDBC. " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de SQL al conectar: " + e.getMessage());
        }
        return conexion;
    }
}