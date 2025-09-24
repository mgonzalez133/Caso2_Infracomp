import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== MENU CASO 2 ===");
            System.out.println("1) Generar referencias (Opción 1)");
            System.out.println("2) Simular ejecución (Opción 2)");
            System.out.println("0) Salir");
            System.out.print("Selecciona una opción: ");
            String op = sc.nextLine().trim();

            switch (op) {
                case "1": 
                    runOption1(sc);
                    break;
                case "2":
                    runOption2(sc);
                    break;
                case "0":
                    System.out.println("¡Chao!");
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    // ===================== Opción 1: Generador =====================
    private static void runOption1(Scanner sc) {
        try {
            Path config = askPath(sc, " Digite la ruta del archivo de configuración (ENTER = config.txt): ", "config.txt");
           
            Path outDir = askPath(sc, "Digite nombre de la carpeta de  los archivos de salida (ENTER = ./src): ", "src");

            
            Generador.generate(config, outDir);

            System.out.println(" Digite el nombre de la carpeta con archivos proc<i>.txt generados en: " + outDir.toAbsolutePath());
        } catch (Throwable t) {
            System.out.println("No se pudo ejecutar la opción 1: " + t.getMessage());
            
        }
    }

    // ===================== Opción 2: Simulador =====================
    private static void runOption2(Scanner sc) {
        try {
            Path tracesDir = askPath(sc, "Carpeta con proc<i>.txt (ENTER = ./src): ", "src");
            int nproc = askInt(sc, "Número de procesos: ");
            int totalFrames = askInt(sc, "Número total de marcos (múltiplo de procesos): ");

            while (nproc <= 0 || totalFrames <= 0 || (totalFrames % nproc != 0)) {
                System.out.println("Regla: totalFrames debe ser múltiplo de nproc y ambos > 0.");
                nproc = askInt(sc, "Número de procesos: ");
                totalFrames = askInt(sc, "Número total de marcos (múltiplo de procesos): ");
            }

            // Verificación rápida de archivos proc<i>.txt
            boolean ok = true;
            for (int i = 0; i < nproc; i++) {
                Path p = tracesDir.resolve("proc" + i + ".txt");
                if (!Files.exists(p)) {
                    System.out.println(" No se encontró: " + p.toAbsolutePath());
                    ok = false;
                }
            }
            if (!ok) {
                System.out.println("Asegúrate de tener proc0.txt, proc1.txt, ... en esa carpeta.");
                return;
            }

            
            Simulador.run(tracesDir, totalFrames, nproc);
        } catch (Throwable t) {
            System.out.println("Error en la simulación: " + t.getMessage());
        }
    }

    // ===================== Helpers =====================
    private static Path askPath(Scanner sc, String prompt, String def) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        if (s.isEmpty()) s = def;
        return Paths.get(s);
    }

    private static int askInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (Exception e) { System.out.println("Ingresa un entero válido."); }
        }
    }
}
