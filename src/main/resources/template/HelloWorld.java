// EIF400-II-2025 - Paradigmas de Programación
// Proyecto Expresso - Sprint Inicial
// UNA - Escuela de Informática

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("=== Expresso Template Ejecutándose ===");

        for (int i = 1; i <= 10; i++) {
            System.out.println("Saludo #" + i + ": ¡Hola desde Expresso transpilado!");
        }

        System.out.println("=== Fin de ejecución ===");
    }

    public static int factorial(int n) {
        return (n <= 1) ? 1 : n * factorial(n - 1);
    }
}