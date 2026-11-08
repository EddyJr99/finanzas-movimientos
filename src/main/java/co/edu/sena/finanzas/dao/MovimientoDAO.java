package co.edu.sena.finanzas.dao;

import co.edu.sena.finanzas.conexion.ConexionBD;
import co.edu.sena.finanzas.modelo.Movimiento;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos (DAO) del modulo Movimientos sobre la base sgfp.
 * Implementa las cuatro operaciones CRUD con JDBC y PreparedStatement,
 * que evita la inyeccion SQL al enviar los valores como parametros.
 *
 * Un movimiento se guarda en dos tablas: "movimientos" y "detalle_categoria".
 * Por eso insertar, actualizar y eliminar usan una transaccion: o se hacen
 * todos los cambios o no se hace ninguno.
 */
public class MovimientoDAO {

    private static final String SQL_INSERTAR_MOVIMIENTO =
            "INSERT INTO movimientos (fecha, descripcion, monto_total, id_cuenta, id_medio_pago) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_categoria (id_movimiento, id_categoria, monto_asignado) "
                    + "VALUES (?, ?, ?)";
    private static final String SQL_ACTUALIZAR_MOVIMIENTO =
            "UPDATE movimientos SET fecha = ?, descripcion = ?, monto_total = ?, "
                    + "id_cuenta = ?, id_medio_pago = ? WHERE id_movimiento = ?";
    private static final String SQL_ELIMINAR_DETALLE =
            "DELETE FROM detalle_categoria WHERE id_movimiento = ?";
    private static final String SQL_ELIMINAR_MOVIMIENTO =
            "DELETE FROM movimientos WHERE id_movimiento = ?";

    /** Consulta base: une el movimiento con los nombres de cuenta, medio de pago y categoria. */
    private static final String SQL_CONSULTA_BASE =
            "SELECT m.id_movimiento, m.fecha, m.descripcion, m.monto_total, "
                    + "m.id_cuenta, c.nombre_cuenta, "
                    + "m.id_medio_pago, mp.nombre_medio_pago, "
                    + "dc.id_categoria, ca.nombre_categoria "
                    + "FROM movimientos m "
                    + "LEFT JOIN cuentas c ON c.id_cuenta = m.id_cuenta "
                    + "LEFT JOIN medios_pago mp ON mp.id_medio_pago = m.id_medio_pago "
                    + "LEFT JOIN detalle_categoria dc ON dc.id_movimiento = m.id_movimiento "
                    + "LEFT JOIN categorias ca ON ca.id_categoria = dc.id_categoria ";
    private static final String SQL_CONSULTAR_TODOS =
            SQL_CONSULTA_BASE + "ORDER BY m.fecha DESC, m.id_movimiento DESC";
    private static final String SQL_CONSULTAR_POR_ID =
            SQL_CONSULTA_BASE + "WHERE m.id_movimiento = ?";

    /**
     * INSERCION: guarda un movimiento nuevo y le asigna su categoria.
     *
     * @return id generado por la base de datos
     */
    public int insertar(Movimiento movimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                int idGenerado = insertarMovimiento(conexion, movimiento);
                insertarDetalle(conexion, idGenerado, movimiento);
                conexion.commit();
                return idGenerado;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    /** CONSULTA: devuelve todos los movimientos, los mas recientes primero. */
    public List<Movimiento> consultarTodos() throws SQLException {
        List<Movimiento> movimientos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                movimientos.add(convertirFila(resultado));
            }
        }
        return movimientos;
    }

    /** CONSULTA: busca un movimiento por su id. */
    public Optional<Movimiento> consultarPorId(int idMovimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR_POR_ID)) {

            sentencia.setInt(1, idMovimiento);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? Optional.of(convertirFila(resultado))
                        : Optional.empty();
            }
        }
    }

    /**
     * ACTUALIZACION: modifica un movimiento existente y su categoria.
     *
     * @return true si el movimiento existia y fue actualizado
     */
    public boolean actualizar(Movimiento movimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                boolean actualizado;
                try (PreparedStatement sentencia =
                             conexion.prepareStatement(SQL_ACTUALIZAR_MOVIMIENTO)) {
                    asignarParametrosMovimiento(sentencia, movimiento);
                    sentencia.setInt(6, movimiento.getIdMovimiento());
                    actualizado = sentencia.executeUpdate() > 0;
                }
                if (actualizado) {
                    eliminarDetalle(conexion, movimiento.getIdMovimiento());
                    insertarDetalle(conexion, movimiento.getIdMovimiento(), movimiento);
                }
                conexion.commit();
                return actualizado;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    /**
     * ELIMINACION: borra un movimiento y su detalle de categoria.
     *
     * @return true si el movimiento existia y fue eliminado
     */
    public boolean eliminar(int idMovimiento) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                eliminarDetalle(conexion, idMovimiento);
                boolean eliminado;
                try (PreparedStatement sentencia =
                             conexion.prepareStatement(SQL_ELIMINAR_MOVIMIENTO)) {
                    sentencia.setInt(1, idMovimiento);
                    eliminado = sentencia.executeUpdate() > 0;
                }
                conexion.commit();
                return eliminado;
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    private int insertarMovimiento(Connection conexion, Movimiento movimiento)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                SQL_INSERTAR_MOVIMIENTO, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametrosMovimiento(sentencia, movimiento);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
                throw new SQLException("La base de datos no devolvio el id generado.");
            }
        }
    }

    /** Asigna toda la cantidad del movimiento a la categoria elegida. */
    private void insertarDetalle(Connection conexion, int idMovimiento, Movimiento movimiento)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_DETALLE)) {
            sentencia.setInt(1, idMovimiento);
            sentencia.setInt(2, movimiento.getIdCategoria());
            sentencia.setBigDecimal(3, movimiento.getMontoTotal());
            sentencia.executeUpdate();
        }
    }

    private void eliminarDetalle(Connection conexion, int idMovimiento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR_DETALLE)) {
            sentencia.setInt(1, idMovimiento);
            sentencia.executeUpdate();
        }
    }

    /** Asigna los cinco campos editables (posiciones 1 a 5) a la sentencia. */
    private void asignarParametrosMovimiento(PreparedStatement sentencia, Movimiento movimiento)
            throws SQLException {
        sentencia.setDate(1, Date.valueOf(movimiento.getFecha()));
        sentencia.setString(2, movimiento.getDescripcion());
        sentencia.setBigDecimal(3, movimiento.getMontoTotal());
        sentencia.setInt(4, movimiento.getIdCuenta());
        sentencia.setInt(5, movimiento.getIdMedioPago());
    }

    /** Convierte la fila actual del ResultSet en un objeto Movimiento. */
    private Movimiento convertirFila(ResultSet resultado) throws SQLException {
        Movimiento movimiento = new Movimiento();
        movimiento.setIdMovimiento(resultado.getInt("id_movimiento"));
        Date fecha = resultado.getDate("fecha");
        movimiento.setFecha(fecha == null ? null : fecha.toLocalDate());
        movimiento.setDescripcion(resultado.getString("descripcion"));
        movimiento.setMontoTotal(resultado.getBigDecimal("monto_total"));
        movimiento.setIdCuenta(resultado.getInt("id_cuenta"));
        movimiento.setNombreCuenta(resultado.getString("nombre_cuenta"));
        movimiento.setIdMedioPago(resultado.getInt("id_medio_pago"));
        movimiento.setNombreMedioPago(resultado.getString("nombre_medio_pago"));
        movimiento.setIdCategoria(resultado.getInt("id_categoria"));
        movimiento.setNombreCategoria(resultado.getString("nombre_categoria"));
        return movimiento;
    }
}
