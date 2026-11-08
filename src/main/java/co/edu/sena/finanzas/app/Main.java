package co.edu.sena.finanzas.app;

import co.edu.sena.finanzas.dao.CatalogoDAO;
import co.edu.sena.finanzas.dao.MovimientoDAO;
import co.edu.sena.finanzas.modelo.Movimiento;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Menu de consola del modulo Movimientos (base de datos sgfp).
 * Permite probar las cuatro funcionalidades: insertar, consultar,
 * actualizar y eliminar movimientos.
 */
public class Main {

    private static final Scanner TECLADO = new Scanner(System.in);
    private static final MovimientoDAO MOVIMIENTO_DAO = new MovimientoDAO();
    private static final CatalogoDAO CATALOGO_DAO = new CatalogoDAO();

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");
            ejecutarOpcion(opcion);
        } while (opcion != 0);
        System.out.println("Hasta pronto.");
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("=== SGFP - Modulo Movimientos ===");
        System.out.println("1. Registrar movimiento");
        System.out.println("2. Listar movimientos");
        System.out.println("3. Buscar movimiento por id");
        System.out.println("4. Actualizar movimiento");
        System.out.println("5. Eliminar movimiento");
        System.out.println("0. Salir");
    }

    private static void ejecutarOpcion(int opcion) {
        try {
            switch (opcion) {
                case 1:
                    registrarMovimiento();
                    break;
                case 2:
                    listarMovimientos();
                    break;
                case 3:
                    buscarMovimiento();
                    break;
                case 4:
                    actualizarMovimiento();
                    break;
                case 5:
                    eliminarMovimiento();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
                    break;
            }
        } catch (SQLIntegrityConstraintViolationException excepcion) {
            System.out.println("Dato rechazado por la base de datos (id inexistente o "
                    + "campo obligatorio vacio): " + excepcion.getMessage());
        } catch (SQLException excepcion) {
            System.out.println("Error de base de datos: " + excepcion.getMessage());
        }
    }

    private static void registrarMovimiento() throws SQLException {
        Movimiento movimiento = leerDatosMovimiento();
        if (movimiento == null) {
            return;
        }
        int idGenerado = MOVIMIENTO_DAO.insertar(movimiento);
        System.out.println("Movimiento registrado con id " + idGenerado + ".");
    }

    private static void listarMovimientos() throws SQLException {
        List<Movimiento> movimientos = MOVIMIENTO_DAO.consultarTodos();
        if (movimientos.isEmpty()) {
            System.out.println("No hay movimientos registrados.");
            return;
        }
        movimientos.forEach(System.out::println);
    }

    private static void buscarMovimiento() throws SQLException {
        int idMovimiento = leerEntero("Id del movimiento: ");
        Optional<Movimiento> movimiento = MOVIMIENTO_DAO.consultarPorId(idMovimiento);
        System.out.println(movimiento.map(Movimiento::toString)
                .orElse("No existe un movimiento con ese id."));
    }

    private static void actualizarMovimiento() throws SQLException {
        int idMovimiento = leerEntero("Id del movimiento a actualizar: ");
        if (MOVIMIENTO_DAO.consultarPorId(idMovimiento).isEmpty()) {
            System.out.println("No existe un movimiento con ese id.");
            return;
        }
        Movimiento movimiento = leerDatosMovimiento();
        if (movimiento == null) {
            return;
        }
        movimiento.setIdMovimiento(idMovimiento);
        boolean actualizado = MOVIMIENTO_DAO.actualizar(movimiento);
        System.out.println(actualizado ? "Movimiento actualizado." : "No se pudo actualizar.");
    }

    private static void eliminarMovimiento() throws SQLException {
        int idMovimiento = leerEntero("Id del movimiento a eliminar: ");
        boolean eliminado = MOVIMIENTO_DAO.eliminar(idMovimiento);
        System.out.println(eliminado ? "Movimiento eliminado." : "No existe un movimiento con ese id.");
    }

    /**
     * Pide por consola los datos de un movimiento, validando cada uno.
     *
     * @return el movimiento, o null si faltan cuentas, medios de pago o categorias
     */
    private static Movimiento leerDatosMovimiento() throws SQLException {
        List<String> cuentas = CATALOGO_DAO.listarCuentas();
        List<String> mediosPago = CATALOGO_DAO.listarMediosPago();
        List<String> categorias = CATALOGO_DAO.listarCategorias();
        if (cuentas.isEmpty() || mediosPago.isEmpty() || categorias.isEmpty()) {
            System.out.println("Antes de registrar movimientos debe existir al menos una "
                    + "cuenta, un medio de pago y una categoria.");
            return null;
        }

        LocalDate fecha = leerFecha();
        String descripcion = leerTextoObligatorio("Descripcion (max. 150 caracteres): ", 150);
        BigDecimal montoTotal = leerMonto();
        int idCuenta = leerIdDeLista("Cuentas disponibles:", cuentas, "Id de la cuenta: ");
        int idMedioPago = leerIdDeLista("Medios de pago disponibles:", mediosPago,
                "Id del medio de pago: ");
        int idCategoria = leerIdDeLista("Categorias disponibles:", categorias,
                "Id de la categoria: ");
        return new Movimiento(fecha, descripcion, montoTotal, idCuenta, idMedioPago, idCategoria);
    }

    /** Muestra una lista de opciones "id - nombre" y pide un id que este en ella. */
    private static int leerIdDeLista(String titulo, List<String> opciones, String mensaje) {
        System.out.println(titulo);
        opciones.forEach(opcion -> System.out.println("  " + opcion));
        while (true) {
            int id = leerEntero(mensaje);
            boolean existe = opciones.stream()
                    .anyMatch(opcion -> opcion.startsWith(id + " - "));
            if (existe) {
                return id;
            }
            System.out.println("Ese id no esta en la lista.");
        }
    }

    private static String leerTextoObligatorio(String mensaje, int longitudMaxima) {
        while (true) {
            System.out.print(mensaje);
            String texto = TECLADO.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("Este campo es obligatorio.");
            } else if (texto.length() > longitudMaxima) {
                System.out.println("Maximo " + longitudMaxima + " caracteres.");
            } else {
                return texto;
            }
        }
    }

    private static BigDecimal leerMonto() {
        while (true) {
            System.out.print("Monto total (mayor que 0, ejemplo 25000.50): ");
            try {
                BigDecimal monto = new BigDecimal(TECLADO.nextLine().trim());
                if (monto.signum() > 0 && monto.scale() <= 2) {
                    return monto;
                }
                System.out.println("El monto debe ser mayor que 0 y tener maximo 2 decimales.");
            } catch (NumberFormatException excepcion) {
                System.out.println("Escriba un numero valido.");
            }
        }
    }

    private static LocalDate leerFecha() {
        while (true) {
            System.out.print("Fecha (AAAA-MM-DD, vacio = hoy): ");
            String entrada = TECLADO.nextLine().trim();
            if (entrada.isEmpty()) {
                return LocalDate.now();
            }
            try {
                LocalDate fecha = LocalDate.parse(entrada);
                if (fecha.isAfter(LocalDate.now())) {
                    System.out.println("La fecha no puede ser futura.");
                } else {
                    return fecha;
                }
            } catch (DateTimeParseException excepcion) {
                System.out.println("Formato invalido. Use AAAA-MM-DD, por ejemplo 2026-11-07.");
            }
        }
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(TECLADO.nextLine().trim());
            } catch (NumberFormatException excepcion) {
                System.out.println("Escriba un numero entero.");
            }
        }
    }
}
