# Estándares de codificación aplicados

| Elemento | Regla | Ejemplos en el proyecto |
|---|---|---|
| Paquetes | Minúsculas, sin guiones bajos, dominio invertido | `co.edu.sena.finanzas.dao`, `co.edu.sena.finanzas.modelo` |
| Clases | PascalCase, sustantivo | `Movimiento`, `MovimientoDAO`, `CatalogoDAO`, `ConexionBD` |
| Métodos | camelCase, empiezan con verbo | `insertar`, `consultarTodos`, `actualizar`, `eliminar` |
| Variables y atributos | camelCase, nombre descriptivo | `idMovimiento`, `montoTotal`, `idMedioPago` |
| Constantes | MAYÚSCULAS con guion bajo | `SQL_INSERTAR_MOVIMIENTO`, `URL_POR_DEFECTO` |
| Tablas y columnas SQL | minúsculas con guion bajo (las de la base sgfp) | `movimientos`, `id_movimiento`, `monto_total` |

Otras prácticas: comentarios Javadoc en clases y métodos públicos, `PreparedStatement` en todas las consultas, `try-with-resources` para cerrar conexiones, transacciones (commit y rollback) en las operaciones que modifican dos tablas y validación de datos en el menú.
