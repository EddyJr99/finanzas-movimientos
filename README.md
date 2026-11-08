# Módulo Movimientos - Sistema de Gestión de Finanzas Personales (SGFP)

Evidencia GA7-220501096-AA2-EV01. Aprendiz: Eddy Alexander Altamar Angulo (Ficha 3336221, ADSO, SENA).

Módulo en Java con conexión JDBC a la base MySQL `sgfp` que permite insertar, consultar, actualizar y eliminar movimientos.

Cada movimiento se guarda en `movimientos` y su categoría en `detalle_categoria`. Esas operaciones se ejecutan en una transacción. Al consultar, se unen `cuentas`, `medios_pago` y `categorias` para mostrar nombres en lugar de ids.

## Cómo ejecutarlo en otro equipo

### Requisitos

- JDK 11 o superior (que `java -version` y `javac -version` funcionen en la terminal)
- MySQL 8.x iniciado (Laragon, XAMPP o MySQL Server) en el puerto 3306

No se necesita Maven: el driver JDBC ya viene en la carpeta `lib/`.

### Paso 1. Crear la base de datos

Ejecutar el script `sql/sgfp_completo.sql`, que crea la base `sgfp`, sus seis tablas y datos de ejemplo. Se puede ejecutar más de una vez sin borrar nada.

- Con DBeaver: abrir el archivo en el editor SQL y ejecutarlo como script (Alt + X).
- Con la consola: `mysql -u root -p < sql/sgfp_completo.sql`

### Paso 2. Revisar la conexión

El programa usa el usuario `root` sin clave en `localhost:3306`, que es la configuración típica de Laragon y XAMPP. Si su MySQL usa otra clave, edite `conexion.properties` (campo `clave`).

### Paso 3. Ejecutar

- Windows: doble clic en `ejecutar.bat`.
- Linux o macOS: `./ejecutar.sh`

El script compila el código y pregunta qué ejecutar:

1. **Prueba automática del CRUD** (`PruebaCrud`): inserta, consulta, actualiza y elimina un movimiento de prueba, e imprime `[OK]` o `[FALLO]` en cada paso. Deja la base como estaba.
2. **Menú interactivo** (`Main`): permite registrar, listar, buscar, actualizar y eliminar movimientos.

### Alternativa con un IDE

Abrir esta carpeta en VS Code (con Extension Pack for Java), IntelliJ IDEA o NetBeans. El archivo `pom.xml` descarga el driver automáticamente si hay internet. Ejecutar `PruebaCrud.java` o `Main.java`.

## Si algo falla

| Mensaje | Qué hacer |
|---|---|
| `Communications link failure` | MySQL no está iniciado o usa otro puerto. Inícielo y revise `conexion.properties`. |
| `Access denied for user 'root'` | Escriba la clave de MySQL en `conexion.properties`. |
| `Unknown database 'sgfp'` | Falta ejecutar `sql/sgfp_completo.sql` (Paso 1). |
| `La tabla ... esta vacia` | Ejecutar `sql/sgfp_completo.sql`, que inserta datos de ejemplo. |

## Estructura

```
src/main/java/co/edu/sena/finanzas/
  modelo/    Movimiento.java
  conexion/  ConexionBD.java
  dao/       MovimientoDAO.java, CatalogoDAO.java
  app/       Main.java, PruebaCrud.java
sql/         sgfp_completo.sql, datos_prueba.sql
lib/         mysql-connector-j-8.4.0.jar (driver JDBC)
conexion.properties   ejecutar.bat   ejecutar.sh   pom.xml
ESTANDARES.md
```

Los estándares de nombres están en `ESTANDARES.md`.

El driver MySQL Connector/J 8.4.0 se distribuye bajo licencia GPLv2 con la excepción FOSS de Oracle y se incluye solo para facilitar la ejecución.
