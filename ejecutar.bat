@echo off
rem Compila y ejecuta el modulo Movimientos en Windows, sin necesitar Maven.
rem Requisitos: JDK 11 o superior (java y javac en el PATH) y MySQL iniciado.
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo No se encontro javac. Instale un JDK 11 o superior y vuelva a intentar.
    pause
    exit /b 1
)

if not exist out mkdir out
javac -encoding UTF-8 -sourcepath src\main\java -cp "lib\*" -d out src\main\java\co\edu\sena\finanzas\app\Main.java src\main\java\co\edu\sena\finanzas\app\PruebaCrud.java
if errorlevel 1 (
    echo Error al compilar.
    pause
    exit /b 1
)

echo.
echo 1 = Prueba automatica del CRUD
echo 2 = Menu interactivo
set /p OPCION=Elija 1 o 2:
echo.
if "%OPCION%"=="2" (
    java -Dfile.encoding=UTF-8 -cp "out;lib\*" co.edu.sena.finanzas.app.Main
) else (
    java -Dfile.encoding=UTF-8 -cp "out;lib\*" co.edu.sena.finanzas.app.PruebaCrud
)
echo.
pause
