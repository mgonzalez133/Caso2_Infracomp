import java.nio.file.*;
import java.io.*;
import java.util.*;

public class Config {
    public final int TP;
    public final int NPROC;
    public final List<int[]> sizes; // cada item: {NF, NC}

    private Config(int TP, int NPROC, List<int[]> sizes) {
        this.TP = TP;
        this.NPROC = NPROC;
        this.sizes = sizes;
    }

    public static Config load(Path p) throws IOException {
        int TP = -1, NPROC = -1;
        String TAMS = null;

        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                String[] kv = line.split("=", 2);
                String k = kv[0].trim().toUpperCase();
                String v = kv[1].trim();
                switch (k) {
                    case "TP": TP = Integer.parseInt(v); break;
                    case "NPROC": NPROC = Integer.parseInt(v); break;
                    case "TAMS": TAMS = v; break;
                }
            }
        }

        if (TP <= 0) throw new IllegalArgumentException("TP inválido en config.");
        if (NPROC <= 0) throw new IllegalArgumentException("NPROC inválido en config.");
        if (TAMS == null || TAMS.isEmpty()) throw new IllegalArgumentException("TAMS faltante en config.");

        // Parsear TAMS=4x4,8x8,...
        String[] parts = TAMS.split(",");
        if (parts.length != NPROC) {
            throw new IllegalArgumentException("TAMS debe listar exactamente " + NPROC + " tamaños (NFxNC).");
        }
        List<int[]> sizes = new ArrayList<>();
        for (String s : parts) {
            s = s.trim().toLowerCase();
            String[] wh = s.split("x");
            if (wh.length != 2) throw new IllegalArgumentException("Formato de tamaño inválido: " + s);
            int nf = Integer.parseInt(wh[0].trim());
            int nc = Integer.parseInt(wh[1].trim());
            if (nf <= 0 || nc <= 0) throw new IllegalArgumentException("NF/NC deben ser >0: " + s);
            sizes.add(new int[]{nf, nc});
        }
        return new Config(TP, NPROC, sizes);
    }
}
