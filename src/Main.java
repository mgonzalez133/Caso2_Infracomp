public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Uso:");
            System.out.println("  java Main gen <config> <outDir>");
            System.out.println("  java Main sim <tracesDir> <totalFrames> <nproc>");
            return;
        }

        switch (args[0]) {
            case "gen":
                Generador.run(args);
                break;
            case "sim":
                Simulador.run(args);
                break;
            default:
                System.out.println("Comando desconocido: " + args[0]);
        }
    }
}
