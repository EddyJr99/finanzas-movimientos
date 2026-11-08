-- Datos de prueba OPCIONALES para la base sgfp (Laragon / DBeaver).
-- Ejecutar solo si las tablas usuarios, cuentas, medios_pago y categorias estan vacias.
-- Supone que los id son AUTO_INCREMENT. Si alguna columna tiene otro nombre o es
-- obligatoria, ajustar este script a la estructura real.

USE sgfp;

INSERT INTO usuarios (nombre, correo, fecha_registro)
VALUES ('Eddy Alexander Altamar Angulo', 'altamar786@gmail.com', CURDATE());

INSERT INTO cuentas (nombre_cuenta, tipo_cuenta, banco, id_usuario)
VALUES ('Cuenta de ahorros', 'Ahorros', 'Bancolombia',
        (SELECT id_usuario FROM usuarios WHERE correo = 'altamar786@gmail.com' LIMIT 1));

INSERT INTO medios_pago (nombre_medio_pago, entidad_financiera)
VALUES ('Tarjeta debito', 'Bancolombia'),
       ('Efectivo', 'Ninguna');

INSERT INTO categorias (nombre_categoria)
VALUES ('Alimentacion'), ('Transporte'), ('Salario');
