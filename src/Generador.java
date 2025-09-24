import java.nio.file.*;
import java.io.*;
import java.util.*;

public class Generador {
    
    private static final boolean PADDED_NAMES = false;

    public static void generate(Path configPath, Path outDir) throws IOException {
        Config cfg = Config.load(configPath);
        Files.createDirectories(outDir);

        System.out.println("== Generador (Opción 1) ==");
        System.out.println("TP=" + cfg.TP + ", NPROC=" + cfg.NPROC + ", TAMS=" + describe(cfg.sizes));

        for (int pid = 0; pid < cfg.NPROC; pid++) {
            int NF = cfg.sizes.get(pid)[0];
            int NC = cfg.sizes.get(pid)[1];
            Path file = outDir.resolve(procName(pid, cfg.NPROC));

            GenResult r = generateOne(cfg.TP, NF, NC, file);

            System.out.println("Escribiendo " + file.toAbsolutePath());
            System.out.println("  NF=" + NF + ", NC=" + NC + ", NR=" + r.NR + ", NP=" + r.NP);
        }

        System.out.println("== Fin generación ==");
    }

    private static String describe(List<int[]> sizes) {
        List<String> s = new ArrayList<>();
        for (int[] wh : sizes) s.add(wh[0] + "x" + wh[1]);
        return String.join(",", s);
    }

    private static String procName(int pid, int nproc) {
        if (!PADDED_NAMES) return "proc" + pid + ".txt";
        int width = String.valueOf(nproc - 1).length();
        return "proc" + String.format("%0" + width + "d", pid) + ".txt";
    }

    private static class GenResult {
        int NR, NP;
        GenResult(int NR, int NP){ this.NR = NR; this.NP = NP; }
    }

    private static GenResult generateOne(int TP, int NF, int NC, Path out) throws IOException {
        final int INT_SIZE = 4;
        int bytesPerMatrix = NF * NC * INT_SIZE;
        int totalBytes = bytesPerMatrix * 3;

        int baseM1 = 0;
        int baseM2 = bytesPerMatrix;
        int baseM3 = bytesPerMatrix * 2;

        int NR = 3 * NF * NC;

        int maxVpn = 0;

        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
           
            int NP = (int)Math.ceil(totalBytes / (double)TP);

            bw.write("TP=" + TP + "\n");
            bw.write("NF=" + NF + "\n");
            bw.write("NC=" + NC + "\n");
            bw.write("NR=" + NR + "\n");
            bw.write("NP=" + NP + "\n");

            // Cuerpo: row-major. Por cada (i,j): M1(r), M2(r), M3(w)
            for (int i = 0; i < NF; i++) {
                for (int j = 0; j < NC; j++) {
                    int idx = i * NC + j;

                    // M1
                    int dv = baseM1 + idx * INT_SIZE;
                    int vpn = dv / TP;
                    int off = dv % TP;
                    if (vpn > maxVpn) maxVpn = vpn;
                    bw.write("M1:[" + i + "-" + j + "]," + vpn + "," + off + ",r\n");

                    // M2
                    dv = baseM2 + idx * INT_SIZE;
                    vpn = dv / TP;
                    off = dv % TP;
                    if (vpn > maxVpn) maxVpn = vpn;
                    bw.write("M2:[" + i + "-" + j + "]," + vpn + "," + off + ",r\n");

                    // M3
                    dv = baseM3 + idx * INT_SIZE;
                    vpn = dv / TP;
                    off = dv % TP;
                    if (vpn > maxVpn) maxVpn = vpn;
                    bw.write("M3:[" + i + "-" + j + "]," + vpn + "," + off + ",w\n");
                }
            }

           
            return new GenResult(NR, NP);
        }
    }
}
