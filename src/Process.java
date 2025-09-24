import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Process {
    public final int id;             
    public final int TP;              
    public final int NF, NC;         
    public final int NP;              
    public final List<Ref> refs;      

    
    public final List<Integer> ownedFrames = new ArrayList<>();
    public final Deque<Integer> freeOwnedFrames = new ArrayDeque<>();

    
    public PageTableEntry[] pt;

    
    public int pc = 0;      
    public int hits = 0;    
    public int faults = 0;
    public int swaps = 0;

    public Process(int id, int TP, int NF, int NC, int NP, List<Ref> refs) {
        this.id = id;
        this.TP = TP;
        this.NF = NF;
        this.NC = NC;
        this.NP = NP;
        this.refs = refs;
        this.pt = new PageTableEntry[Math.max(1, NP)];
        for (int i = 0; i < this.pt.length; i++) this.pt[i] = new PageTableEntry();
    }

    public boolean finished() { return pc >= refs.size(); }

    
    public static Process loadFromFile(int id, Path p) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(p)) {
            int TP = 0, NF = 0, NC = 0, NR = 0, NP = 0;
            String line;

            
            for (int k = 0; k < 5; k++) {
                line = br.readLine();
                if (line == null) throw new IOException("Encabezado incompleto en " + p);
                line = line.trim();
                if (line.startsWith("TP=")) TP = Integer.parseInt(line.substring(3).trim());
                else if (line.startsWith("NF=")) NF = Integer.parseInt(line.substring(3).trim());
                else if (line.startsWith("NC=")) NC = Integer.parseInt(line.substring(3).trim());
                else if (line.startsWith("NR=")) NR = Integer.parseInt(line.substring(3).trim());
                else if (line.startsWith("NP=")) NP = Integer.parseInt(line.substring(3).trim());
            }

            List<Ref> refs = new ArrayList<>();
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                
                String[] parts = line.split(",");
                String p0 = parts[0]; // M1:[0-0]
                int matrix = Integer.parseInt(p0.substring(1, 2));
                int lb = p0.indexOf('['), dash = p0.indexOf('-'), rb = p0.indexOf(']');
                int i = Integer.parseInt(p0.substring(lb + 1, dash));
                int j = Integer.parseInt(p0.substring(dash + 1, rb));
                int vpn = Integer.parseInt(parts[1].trim());
                int off = Integer.parseInt(parts[2].trim());
                char op = parts[3].trim().charAt(0);

                refs.add(new Ref(matrix, i, j, vpn, off, op));
                count++;
            }

            if (NR != 0 && count != NR) {
                System.out.println("Advertencia: NR=" + NR + " pero se leyeron " + count + " referencias en " + p);
            }

            return new Process(id, TP, NF, NC, NP, refs);
        }
    }
}

