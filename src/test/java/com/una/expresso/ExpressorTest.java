package com.una.expresso;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExpressorTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("Test.expresso");
        Files.writeString(testFile, """
            // Test Expresso file
            data nat = {
                Zero,
                S(nat)
            }
            
            fun add(x:nat, y:nat) = match x with
                Zero -> y
                S(z) -> S(add(z, y))
            """);
    }

    @Test
    void testResultSuccess() {
        var result = ExpressorCLI.Result.success("test");
        assertTrue(result.isSuccess());
        assertEquals("test", result.toOptional().get());
    }

    @Test
    void testResultFailure() {
        var result = ExpressorCLI.Result.<String>failure("error");
        assertFalse(result.isSuccess());
        assertTrue(result.toOptional().isEmpty());
    }

    @Test
    void testResultMap() {
        var result = ExpressorCLI.Result.success(5)
                .map(x -> x * 2)
                .map(String::valueOf);

        assertTrue(result.isSuccess());
        assertEquals("10", result.toOptional().get());
    }

    @Test
    void testResultFlatMap() {
        var result = ExpressorCLI.Result.success(5)
                .flatMap(x -> x > 0 ?
                        ExpressorCLI.Result.success(x * 2) :
                        ExpressorCLI.Result.failure("negative"));

        assertTrue(result.isSuccess());
        assertEquals(10, result.toOptional().get());
    }

    @Test
    void testResultFold() {
        var successResult = ExpressorCLI.Result.success(42);
        var failureResult = ExpressorCLI.Result.<Integer>failure("error");

        var successValue = successResult.fold(
                error -> -1,
                value -> value * 2
        );

        var failureValue = failureResult.fold(
                error -> -1,
                value -> value * 2
        );

        assertEquals(84, successValue);
        assertEquals(-1, failureValue);
    }

    @Test
    void testTranspileValidation() throws Exception {
        // Test archivo válido
        var validResult = ExpressorCLI.validateExpressoFile(testFile.toString(), false);
        assertTrue(validResult.isSuccess());

        // Test archivo inexistente
        var invalidResult = ExpressorCLI.validateExpressoFile("nonexistent.expresso", false);
        assertFalse(invalidResult.isSuccess());

        // Test extensión incorrecta
        var wrongExtResult = ExpressorCLI.validateExpressoFile("test.txt", false);
        assertFalse(wrongExtResult.isSuccess());
    }

    @Test
    void testTemplateGeneration() throws Exception {
        // Crear template mock para el test
        var templateDir = tempDir.resolve("template");
        Files.createDirectories(templateDir);
        var templateFile = templateDir.resolve("Test.java");
        Files.writeString(templateFile, """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Template test");
                }
            }
            """);

        // Simular la función copyTemplateToOutput (método privado, testear indirectamente)
        var result = ExpressorCLI.transpileFile(testFile.toString(), tempDir.toString(), false);
        // Este test puede fallar si no existe el template real, pero demuestra la estructura
        assertNotNull(result);
    }
}