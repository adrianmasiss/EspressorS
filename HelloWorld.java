// Código Java generado automáticamente desde Expresso
// Sprint 1 - Mock implementation

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("este no es el archivo");
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
