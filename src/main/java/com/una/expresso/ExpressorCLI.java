package com.una.expresso;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Command(
        name = "expressor",
        version = "1.0",
        description = "Transpilador de Expresso a Java",
        subcommands = {
                ExpressorCLI.TranspileCommand.class,
                ExpressorCLI.BuildCommand.class,
                ExpressorCLI.RunCommand.class
        }
)
public class ExpressorCLI implements Runnable {

    public static void main(String[] args) {
        var commandLine = new CommandLine(new ExpressorCLI());
        var exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Mostrar ayuda cuando se ejecuta sin subcomandos
        CommandLine.usage(this, System.out);
    }

    // Subcomando transpile
    @Command(name = "transpile", description = "Transpila un archivo Expresso a Java")
    static class TranspileCommand implements Runnable {
        @Parameters(index = "0", description = "Archivo fuente Expresso")
        private String sourceFile;

        @Option(names = {"--out"}, description = "Directorio de salida", defaultValue = ".")
        private String outputDir;

        @Option(names = {"--verbose"}, description = "Mostrar pasos detallados")
        private boolean verbose;

        @Override
        public void run() {
            var result = transpileFile(sourceFile, outputDir, verbose);
            result.fold(
                    error -> {
                        System.err.println("Error en transpilación: " + error);
                        System.exit(1);
                        return null;
                    },
                    success -> {
                        System.out.println("Transpilación exitosa: " + success);
                        return success;
                    }
            );
        }
    }

    // Subcomando build
    @Command(name = "build", description = "Transpila y compila un archivo Expresso")
    static class BuildCommand implements Runnable {
        @Parameters(index = "0", description = "Archivo fuente Expresso")
        private String sourceFile;

        @Option(names = {"--out"}, description = "Directorio de salida", defaultValue = ".")
        private String outputDir;

        @Option(names = {"--verbose"}, description = "Mostrar pasos detallados")
        private boolean verbose;

        @Override
        public void run() {
            var pipeline = createBuildPipeline(sourceFile, outputDir, verbose);
            var result = pipeline.get();

            result.fold(
                    error -> {
                        System.err.println("Error en build: " + error);
                        System.exit(1);
                        return null;
                    },
                    success -> {
                        System.out.println("Build exitoso: " + success);
                        return success;
                    }
            );
        }
    }

    // Subcomando run
    @Command(name = "run", description = "Transpila, compila y ejecuta un archivo Expresso")
    static class RunCommand implements Runnable {
        @Parameters(index = "0", description = "Archivo fuente Expresso")
        private String sourceFile;

        @Option(names = {"--out"}, description = "Directorio de salida", defaultValue = ".")
        private String outputDir;

        @Option(names = {"--verbose"}, description = "Mostrar pasos detallados")
        private boolean verbose;

        @Override
        public void run() {
            var pipeline = createRunPipeline(sourceFile, outputDir, verbose);
            var result = pipeline.get();

            result.fold(
                    error -> {
                        System.err.println("Error en ejecución: " + error);
                        System.exit(1);
                        return null;
                    },
                    success -> {
                        System.out.println("Ejecución completada exitosamente");
                        return success;
                    }
            );
        }
    }

    // Funciones de utilidad usando programación funcional

    /**
     * Transpila un archivo Expresso a Java usando template
     */
    static Result<String> transpileFile(String sourceFile, String outputDir, boolean verbose) {
        return validateExpressoFile(sourceFile, verbose)
                .flatMap(path -> copyTemplateToOutput(path, outputDir, verbose));
    }

    /**
     * Pipeline funcional para build
     */
    private static Supplier<Result<String>> createBuildPipeline(String sourceFile, String outputDir, boolean verbose) {
        return () -> transpileFile(sourceFile, outputDir, verbose)
                .flatMap(javaFile -> compileJavaFile(javaFile, verbose));
    }

    /**
     * Pipeline funcional para run
     */
    private static Supplier<Result<String>> createRunPipeline(String sourceFile, String outputDir, boolean verbose) {
        return () -> createBuildPipeline(sourceFile, outputDir, verbose).get()
                .flatMap(classFile -> executeClass(classFile, verbose));
    }

    /**
     * Valida que el archivo Expresso existe y no está vacío
     */
    static Result<Path> validateExpressoFile(String filename, boolean verbose) {
        if (verbose) System.out.println("Leyendo archivo: " + filename);

        var path = Paths.get(filename);
        if (!Files.exists(path)) {
            return Result.failure("Archivo no encontrado: " + filename);
        }
        if (!filename.endsWith(".expresso")) {
            return Result.failure("El archivo debe tener extensión .expresso");
        }

        try {
            var content = Files.readString(path).trim();
            if (content.isEmpty()) {
                return Result.failure("El archivo está vacío: " + filename);
            }
            if (verbose) System.out.println("Archivo leído correctamente");
            return Result.success(path);
        } catch (IOException e) {
            return Result.failure("Error leyendo archivo: " + e.getMessage());
        }
    }

    /**
     * Copia template desde resources/template/ a directorio de salida
     */
    private static Result<String> copyTemplateToOutput(Path expressoPath, String outputDir, boolean verbose) {
        try {
            if (verbose) System.out.println("Transpilando...");

            var className = getClassName(expressoPath);
            var templatePath = Paths.get("src/main/resources/template", className + ".java");

            // Si no existe template específico, usar template genérico
            if (!Files.exists(templatePath)) {
                templatePath = Paths.get("src/main/resources/template/HelloWorld.java");
            }

            if (!Files.exists(templatePath)) {
                return Result.failure("Template no encontrado: " + templatePath);
            }

            // Crear directorio de salida si no existe
            var outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                if (verbose) System.out.println("Creado directorio: " + outputDir);
            }

            var javaFile = outputPath.resolve(className + ".java");
            var templateContent = Files.readString(templatePath);

            // Reemplazar nombre de clase si es necesario
            var finalContent = templateContent.replaceAll("HelloWorld", className);
            Files.writeString(javaFile, finalContent);

            if (verbose) System.out.println("Archivo transpilado a: " + javaFile);
            return Result.success(javaFile.toString());
        } catch (IOException e) {
            return Result.failure("Error copiando template: " + e.getMessage());
        }
    }

    /**
     * Compila archivo Java usando javax.tools.JavaCompiler
     */
    private static Result<String> compileJavaFile(String javaFile, boolean verbose) {
        if (verbose) System.out.println("Compilando: " + javaFile);

        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return Result.failure("Compilador Java no encontrado");
        }

        var result = compiler.run(null, null, null, javaFile);
        if (result == 0) {
            var classFile = javaFile.replace(".java", ".class");
            if (verbose) System.out.println("Compilado exitosamente: " + classFile);
            return Result.success(classFile);
        } else {
            return Result.failure("Error compilando " + javaFile);
        }
    }

    /**
     * Ejecuta clase Java compilada
     */
    private static Result<String> executeClass(String classFile, boolean verbose) {
        try {
            if (verbose) System.out.println("Ejecutando: " + classFile);

            var classPath = Paths.get(classFile);                           // ← NUEVO
            var className = classPath.getFileName().toString().replace(".class", "");
            var workingDir = classPath.getParent().toFile();                // ← NUEVO

            var processBuilder = new ProcessBuilder("java", className);
            processBuilder.directory(workingDir);                           // ← NUEVO - Esto es clave
            processBuilder.inheritIO(); // Para ver la salida del programa

            var process = processBuilder.start();
            var exitCode = process.waitFor();

            if (exitCode == 0) {
                return Result.success("Ejecución exitosa");
            } else {
                return Result.failure("Error en ejecución, código: " + exitCode);
            }
        } catch (Exception e) {
            return Result.failure("Error ejecutando clase: " + e.getMessage());
        }
    }

    /**
     * Extrae nombre de clase del path
     */
    private static String getClassName(Path path) {
        var filename = path.getFileName().toString();
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    /**
     * Genera contenido Java mock para Sprint 1
     */
    private static String generateMockJavaContent(String className) {
        return String.format("""
            // Código Java generado automáticamente desde Expresso
            // Sprint 1 - Mock implementation
            
            public class %s {
                public static void main(String[] args) {
                    System.out.println("Hello from %s - transpiled from Expresso!");
                    System.out.println("This is a mock implementation for Sprint 1");
                }
                
                // Mock de una función simple
                public static int add(int a, int b) {
                    return a + b;
                }
                
                // Mock de pattern matching simple
                public static String processNat(Object nat) {
                    return switch(nat) {
                        case Integer i when i == 0 -> "Zero";
                        case Integer i when i > 0 -> "S(" + processNat(i - 1) + ")";
                        default -> "Unknown";
                    };
                }
            }
            """, className, className);
    }

    /**
     * Clase Result para manejo funcional de errores (Similar a Either en Haskell)
     */
    public static abstract sealed class Result<T> permits Result.Success, Result.Failure {

        public static <T> Result<T> success(T value) {
            return new Success<>(value);
        }

        public static <T> Result<T> failure(String error) {
            return new Failure<>(error);
        }

        public abstract <U> Result<U> map(Function<T, U> mapper);

        public abstract <U> Result<U> flatMap(Function<T, Result<U>> mapper);

        public abstract <U> U fold(Function<String, U> onFailure, Function<T, U> onSuccess);

        public abstract boolean isSuccess();

        public abstract Optional<T> toOptional();

        public static final class Success<T> extends Result<T> {
            private final T value;

            private Success(T value) {
                this.value = value;
            }

            @Override
            public <U> Result<U> map(Function<T, U> mapper) {
                try {
                    return success(mapper.apply(value));
                } catch (Exception e) {
                    return failure("Error en map: " + e.getMessage());
                }
            }

            @Override
            public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
                try {
                    return mapper.apply(value);
                } catch (Exception e) {
                    return failure("Error en flatMap: " + e.getMessage());
                }
            }

            @Override
            public <U> U fold(Function<String, U> onFailure, Function<T, U> onSuccess) {
                return onSuccess.apply(value);
            }

            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public Optional<T> toOptional() {
                return Optional.of(value);
            }
        }

        public static final class Failure<T> extends Result<T> {
            private final String error;

            private Failure(String error) {
                this.error = error;
            }

            @Override
            public <U> Result<U> map(Function<T, U> mapper) {
                return failure(error);
            }

            @Override
            public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
                return failure(error);
            }

            @Override
            public <U> U fold(Function<String, U> onFailure, Function<T, U> onSuccess) {
                return onFailure.apply(error);
            }

            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public Optional<T> toOptional() {
                return Optional.empty();
            }
        }
    }
}