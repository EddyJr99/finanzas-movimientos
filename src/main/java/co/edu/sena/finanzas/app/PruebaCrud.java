package co.edu.sena.finanzas.app;

import co.edu.sena.finanzas.dao.CatalogoDAO;
import co.edu.sena.finanzas.dao.MovimientoDAO;
import co.edu.sena.finanzas.modelo.Movimiento;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Prueba automatica del modulo Movimientos contra la base sgfp.
 * Ejecuta en orden insercion, consulta, actualizacion y eliminacion de un
 * movimiento de prueba, e imprime el resultado de cada paso. Al terminar la
 * base queda como estaba, porque el movimiento de prueba se elimina.
 */
public class PruebaCrud {

    private static final MovimientoDAO MOVIMIENTO_DAO = new MovimientoDAO();
    private static final CatalogoDAO CATALOGO_DAO = new CatalogoDAO();

    public static void main(String[] args) {
        System.out.println("=== PRUEBA CRUD - Modulo Movimientos (sgfp) ===");
        try {
            int idCuenta = primerId(CATALOGO_DAO.listarCuentas(), "cuentas");
            int idMedioPago = primerId(CATALOGO_DAO.listarMediosPago(), "medios_pago");
            int idCategoria = primerId(CATALOGO_DAO.listarCategorias(), "categorias");

            // 1. INSERCION
            Movimiento nuevo = new Movimiento(LocalDate.now(), "Movimiento de prueba",
                    new BigDecimal("10000.00"), idCuenta, idMedioPago, idCategoria);
            int idMovimiento = MOVIMIENTO_DAO.insertar(nuevo);
            mostrarResultado("1. INSERCION", idMovimiento > 0, "id generado = " + idMovimiento);

            // 2. CONSULTA
            Optional<Movimiento> consultado = MOVIMIENTO_DAO.consultarPorId(idMovimiento);
            mostrarResultado("2. CONSULTA por id", consultado.isPresent(),
                    consultado.map(Movimiento::toString).orElse("no se encontro"));
            List<Movimiento> todos = MOVIMIENTO_DAO.consultarTodos();
            mostrarResultado("2. CONSULTA de todos", !todos.isEmpty(),
                    todos.size() + " movimiento(s) en la base");

            // 3. ACTUALIZACION
            Movimiento cambio = new Movimiento(LocalDate.now(), "Movimiento de prueba ACTUALIZADO",
                    new BigDecimal("25000.50"), idCuenta, idMedioPago, idCategoria);
            cambio.setIdMovimiento(idMovimiento);
            boolean actualizado = MOVIMIENTO_DAO.actualizar(cambio);
            Optional<Movimiento> tras = MOVIMIENTO_DAO.consultarPorId(idMovimiento);
            boolean valorCambio = tras.isPresent()
                    && tras.get().getMontoTotal().compareTo(new BigDecimal("25000.50")) == 0;
            mostrarResultado("3. ACTUALIZACION", actualizado && valorCambio,
                    tras.map(Movimiento::toString).orElse("no se encontro"));

            // 4. ELIMINACION
            boolean eliminado = MOVIMIENTO_DAO.eliminar(idMovimiento);
            boolean yaNoExiste = MOVIMIENTO_DAO.consultarPorId(idMovimiento).isEmpty();
            mostrarResultado("4. ELIMINACION", eliminado && yaNoExiste,
                    "el movimiento " + idMovimiento + " ya no existe");

            System.out.println("=== FIN DE LA PRUEBA ===");
        } catch (SQLException excepcion) {
            System.out.println("ERROR de base de datos: " + excepcion.getMessage());
        } catch (IllegalStateException excepcion) {
            System.out.println("ERROR: " + excepcion.getMessage());
        }
    }

    /** Toma el id de la primera fila de una lista con formato "id - nombre". */
    private static int primerId(List<String> elementos, String tabla) {
        if (elementos.isEmpty()) {
            throw new IllegalStateException("La tabla " + tabla
                    + " esta vacia. Agregue al menos un registro (ver sql/datos_prueba.sql).");
        }
        return Integer.parseInt(elementos.get(0).split(" - ")[0]);
    }

    private static void mostrarResultado(String paso, boolean exito, String detalle) {
        System.out.println((exito ? "[OK]    " : "[FALLO] ") + paso + " -> " + detalle);
    }
}
