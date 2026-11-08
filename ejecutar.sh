#!/bin/sh
# Compila y ejecuta el modulo Movimientos en Linux o macOS, sin necesitar Maven.
# Requisitos: JDK 11 o superior (java y javac en el PATH) y MySQL iniciado.
cd "$(dirname "$0")" || exit 1

if ! command -v javac >/dev/null 2>&1; then
    echo "No se encontro javac. Instale un JDK 11 o superior y vuelva a intentar."
    exit 1
fi

mkdir -p out
javac -encoding UTF-8 -sourcepath src/main/java -cp "lib/*" -d out \
    src/main/java/co/edu/sena/finanzas/app/Main.java \
    src/main/java/co/edu/sena/finanzas/app/PruebaCrud.java || exit 1

echo
echo "1 = Prueba automatica del CRUD"
echo "2 = Menu interactivo"
printf "Elija 1 o 2: "
read -r OPCION
echo

if [ "$OPCION" = "2" ]; then
    java -Dfile.encoding=UTF-8 -cp "out:lib/*" co.edu.sena.finanzas.app.Main
else
    java -Dfile.encoding=UTF-8 -cp "out:lib/*" co.edu.sena.finanzas.app.PruebaCrud
fi
