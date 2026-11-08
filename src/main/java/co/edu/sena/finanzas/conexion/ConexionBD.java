package co.edu.sena.finanzas.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Administra la conexion JDBC con la base de datos MySQL "sgfp" (Laragon).
 *
 * Los datos de conexion se pueden cambiar sin tocar el codigo usando variables
 * de entorno: SGFP_DB_URL, SGFP_DB_USER y SGFP_DB_PASSWORD.
 * Por defecto usa MySQL local de Laragon: usuario root sin clave.
 */
public final class ConexionBD {

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/sgfp"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String USUARIO_POR_DEFECTO = "root";
    private static final String CLAVE_POR_DEFECTO = "";

    /** Clase de utilidad: no se instancia. */
    private ConexionBD() {
    }

    /**
     * Abre una nueva conexion. Quien la solicita debe cerrarla,
     * idealmente con try-with-resources.
     *
     * @return conexion abierta a la base de datos
     * @throws SQLException si no se logra conectar
     */
    public static Connection obtenerConexion() throws SQLException {
        String url = leerVariable("SGFP_DB_URL", URL_POR_DEFECTO);
        String usuario = leerVariable("SGFP_DB_USER", USUARIO_POR_DEFECTO);
        String clave = leerVariable("SGFP_DB_PASSWORD", CLAVE_POR_DEFECTO);
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Devuelve el valor de una variable de entorno o el valor por defecto. */
    private static String leerVariable(String nombre, String valorPorDefecto) {
        String valor = System.getenv(nombre);
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor;
    }
}
