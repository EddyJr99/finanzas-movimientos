# Módulo Movimientos - Sistema de Gestión de Finanzas Personales (SGFP)

Evidencia GA7-220501096-AA2-EV01. Aprendiz: Eddy Alexander Altamar Angulo (Ficha 3336221, ADSO, SENA).

Módulo en Java con conexión JDBC a la base MySQL `sgfp` (Laragon) que permite insertar, consultar, actualizar y eliminar movimientos.

Cada movimiento se guarda en `movimientos` y su categoría en `detalle_categoria`. Esas operaciones se ejecutan en una transacción. Al consultar, se unen `cuentas`, `medios_pago` y `categorias` para mostrar nombres en lugar de ids.

## Requisitos

- JDK 17 o superior
- Maven 3.8 o superior (o el driver `mysql-connector-j` agregado al IDE)
- Laragon con MySQL iniciado y la base `sgfp`

## Cómo ejecutarlo

1. Iniciar MySQL en Laragon.
2. Verificar en DBeaver que existan datos en `cuentas`, `medios_pago` y `categorias`. Si están vacías, ejecutar `sql/datos_prueba.sql`.
3. Si MySQL no usa `root` sin clave, definir las variables de entorno `SGFP_DB_USER` y `SGFP_DB_PASSWORD` (opcionalmente `SGFP_DB_URL`).
4. Compilar y ejecutar:

```bash
mvn clean package
java -jar target/finanzas-movimientos-1.0.0.jar
```

## Estructura

```
src/main/java/co/edu/sena/finanzas/
  modelo/    Movimiento.java
  conexion/  ConexionBD.java
  dao/       MovimientoDAO.java, CatalogoDAO.java
  app/       Main.java
sql/         datos_prueba.sql
ESTANDARES.md
```

Los estándares de nombres están en `ESTANDARES.md`.
