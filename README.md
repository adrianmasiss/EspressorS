Expresso - Transpilador de Minilenguaje Funcional a Java
Proyecto: EIF400-II-2025 - Paradigmas de Programación
Curso: Paradigmas de Programación
Escuela: Informática - Universidad Nacional (UNA)
Autores:
-Valery Alfaro Morales
-Estrella Medrano Caceres
-Eduardo Cantón Grajales
-Adrian Masis Salazar
-Jose Valerio Rodriguez

Grupo: 05-1pm
Sprint: #1
Fecha: 31 de Agosto del 2025
Descripción del Proyecto
Expresso es un transpilador que convierte código de un minilenguaje funcional a Java moderno. Este proyecto académico combina:

Construcción de lenguajes y paradigmas de programación.
Herramientas profesionales como ANTLR4, testing unitario y control de versiones
Diseño de software colaborativo estructurado


Ejemplo de Transpilación
Código Expresso:
data nat = {
    Zero,
    S(nat)
}

fun sum(x:nat, y:nat) = match x with
    Zero -> y
    S(z) -> S(sum(z, y))
	
	
	
Código Java generado:
sealed interface Nat permits Zero, S {}
record Zero() implements Nat {}
record S(Nat pred) implements Nat {}

static Nat sum(Nat x, Nat y) {
    return switch (x) {
        case Zero z -> y;
        case S(var pred) -> new S(sum(pred, y));
    };
}

Esta es una implementación mock para el Sprint Inicial. Las funcionalidades de transpilación real se implementarán en sprints posteriores.

Funcionalidades Implementadas:

CLI con PicoCLI - Interfaz de línea de comandos profesional
Comando transpile - Mock que usa templates predefinidos
Comando build - Transpilación + compilación a .class
Comando run - Transpilación + compilación + ejecución
Manejo funcional de errores - Usando mónada Result<T>
Validación de archivos - Verificación de existencia y contenido
Gestión de directorios - Creación automática de carpetas de salida

Requisitos del Sistema:

Java: JDK 23
Maven: 3.8+
SO: Windows


Uso:
Transpilación:
.\expressor\expressor transpile [opciones] <archivo.expresso>
Build:
.\expressor\expressor build [opciones] <archivo.expresso>
Run:
.\expressor\expressor run [opciones] <archivo.expresso>

Ejemplo:
Transpilación:
.\expressor\expressor transpile --out output --verbose HelloWorld.expresso
Build:
.\expressor\expressor build --out output --verbose HelloWorld.expresso
Run:
.\expressor\expressor run --out output --verbose HelloWorld.expresso

Opciones Disponibles

--out <directorio> - Directorio de salida (default: directorio actual)
--verbose - Mostrar pasos detallados del proceso
--help - Mostrar ayuda del comando


Crear Ejecutable Nativo (Opcional)
Para crear un ejecutable expressor que funcione sin java -jar:

jpackage.exe ^
  --name expressor ^
  --input target ^
  --main-jar expresso-1.0-SNAPSHOT-shaded.jar ^
  --main-class com.una.expresso.ExpressorCLI ^
  --type app-image ^
  --win-console


# Usar el ejecutable nativo
.\expressor\expressor transpile HelloWorld.expresso
