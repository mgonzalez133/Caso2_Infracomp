import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Simulador {
    public static void run(Path tracesDir, int totalFrames, int nproc) throws IOException {
        // === 1) Cargar procesos ===
        List<Process> procs = new ArrayList<>();
        for (int pid = 0; pid < nproc; pid++) {
            Path pf = tracesDir.resolve("proc" + pid + ".txt");
            System.out.println("PROC " + pid + " == Leyendo archivo de configuración ==");
            Process p = Process.loadFromFile(pid, pf);
            System.out.println("PROC " + pid + "leyendo TP. Tam Páginas: " + p.TP);
            System.out.println("PROC " + pid + "leyendo NF. Num Filas: " + p.NF);
            System.out.println("PROC " + pid + "leyendo NC. Num Cols: " + p.NC);
            System.out.println("PROC " + pid + "leyendo NR. Num Referencias: " + p.refs.size());
            System.out.println("PROC " + pid + "leyendo NP. Num Paginas: " + p.pt.length);
            System.out.println("PROC " + pid + "== Terminó de leer archivo de configuración ==");
            procs.add(p);
        }

        // === 2) Asignar marcos equitativamente ===
        if (totalFrames % nproc != 0) {
            throw new IllegalArgumentException("totalFrames debe ser múltiplo de nproc");
        }
        MemoryManager mm = new MemoryManager(totalFrames);
        int perProc = totalFrames / nproc;
        for (Process p : procs) {
            List<Integer> ids = mm.assignFramesToProcess(p.id, perProc);
            for (int fid : ids) {
                System.out.println("Proceso " + p.id + ": recibe marco " + fid);
                p.ownedFrames.add(fid);
                p.freeOwnedFrames.add(fid);
            }
        }

        // === 3) Round-robin con cola ===
        Deque<Process> cola = new ArrayDeque<>(procs);
        MemoryManager.StatsAcc acc = new MemoryManager.StatsAcc();

        while (!cola.isEmpty()) {
            Process p = cola.pollFirst();

            // ¿Terminó?
            if (p.finished()) {
                System.out.println("========================");
                System.out.println("Termino proc: " + p.id);
                System.out.println("========================");

                // Liberar marcos y reasignar
                int liberar = p.ownedFrames.size();
                mm.releaseFramesOfProcess(p.id);
                p.ownedFrames.clear();
                p.freeOwnedFrames.clear();

                // Elegir destino: el vivo con más fallos
                Process target = null;
                for (Process cand : cola) {
                    if (target == null || cand.faults > target.faults
                        || (cand.faults == target.faults && cand.id < target.id)) {
                        target = cand;
                    }
                }
                if (target != null && liberar > 0) {
                    List<Integer> got = mm.assignFramesToProcess(target.id, liberar);
                    for (int fid : got) {
                        System.out.println("PROC " + target.id + " asignando marco nuevo " + fid);
                        target.ownedFrames.add(fid);
                        target.freeOwnedFrames.add(fid);
                    }
                }
                continue; // no reencolar p
            }

            // Turno de p: procesa 1 referencia
            System.out.println("Turno proc: " + p.id);
            Ref r = p.refs.get(p.pc);
            System.out.println("PROC " + p.id + " analizando linea_: " + p.pc);

            boolean hit = mm.access(p, r, acc, true);

            if (hit) {
                // Solo en hit consumimos la referencia
                p.pc++;
            }
            System.out.println("PROC " + p.id + " envejecimiento");

            // Reencolar si sigue vivo
            if (!p.finished()) cola.addLast(p);
        }

        // === 4) Estadísticas finales ===
        for (Process p : procs) {
            int refs = p.refs.size();
            int hits = p.hits;
            int faults = p.faults;
            int swaps = p.swaps;
            double tasaF = refs == 0 ? 0.0 : (faults * 1.0) / refs;
            double tasaE = refs == 0 ? 0.0 : (hits * 1.0) / refs;
            System.out.println("Proceso: " + p.id);
            System.out.println("- Num referencias: " + refs);
            System.out.println("- Fallas: " + faults);
            System.out.println("- Hits: " + hits);
            System.out.println("- SWAP: " + swaps);
            System.out.printf("- Tasa fallas: %.4f%n", tasaF);
            System.out.printf("- Tasa éxito: %.4f%n", tasaE);
        }
    }
}

