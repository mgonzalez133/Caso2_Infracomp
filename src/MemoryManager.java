import java.util.*;

public class MemoryManager {
    private final Frame[] ram;   // RAM simulada
    private long tick = 0;       // contador global para LRU

    public MemoryManager(int totalFrames){
        ram = new Frame[totalFrames];
        for (int i = 0; i < totalFrames; i++) ram[i] = new Frame(i);
    }

    public int totalFrames(){ return ram.length; }

    
    public List<Integer> assignFramesToProcess(int procId, int howMany){
        List<Integer> ids = new ArrayList<>();
        for (Frame f : ram){
            if (f.free){
                f.free = false;
                f.ownerProc = procId;
                f.vpn = -1;
                f.lastAccess = -1;
                ids.add(f.id);
                if (ids.size() == howMany) break;
            }
        }
        return ids;
    }

    
    public void releaseFramesOfProcess(int procId){
        for (Frame f : ram){
            if (!f.free && f.ownerProc == procId){
                f.free = true;
                f.ownerProc = -1;
                f.vpn = -1;
                f.lastAccess = -1;
            }
        }
    }

    private Frame frameById(int id){ return ram[id]; }

    
    public boolean access(Process p, Ref r, StatsAcc acc, boolean verbose){
        tick++;
        int vpn = r.vpn;

        
        if (vpn < p.pt.length && p.pt[vpn].valid){
            int fid = p.pt[vpn].frameId;
            Frame f = frameById(fid);
            f.lastAccess = tick;
            p.hits++;
            if (verbose) System.out.println("PROC " + p.id + " hits: " + p.hits);
            return true;
        }

        
        if (verbose) System.out.println("PROC " + p.id + " falla de pag: " + p.faults);
        p.faults++;

        
        Integer fidFree = p.freeOwnedFrames.pollFirst();
        if (fidFree != null){
            Frame f = frameById(fidFree);
            f.vpn = vpn;
            f.lastAccess = tick;

            ensurePTSize(p, vpn);
            p.pt[vpn].valid = true;
            p.pt[vpn].frameId = fidFree;

            // SWAP: solo entrada (sin reemplazo)
            p.swaps += 1;
            acc.swaps += 1;
            return false;
        }

        
        int victimId = -1;
        long best = Long.MAX_VALUE;
        for (int fid : p.ownedFrames){
            Frame f = frameById(fid);
            if (f.vpn == -1){ victimId = fid; break; } // si hubiera uno vacío por sanidad
            if (f.lastAccess < best){ best = f.lastAccess; victimId = fid; }
        }
        Frame victim = frameById(victimId);

        
        if (victim.vpn >= 0 && victim.vpn < p.pt.length){
            p.pt[victim.vpn].valid = false;
            p.pt[victim.vpn].frameId = -1;
        }

       
        victim.vpn = vpn;
        victim.lastAccess = tick;

        ensurePTSize(p, vpn);
        p.pt[vpn].valid = true;
        p.pt[vpn].frameId = victimId;

        
        p.swaps += 2;
        acc.swaps += 2;

        return false;
    }

    
    private void ensurePTSize(Process p, int vpn){
        if (vpn >= p.pt.length){
            PageTableEntry[] npt = new PageTableEntry[vpn + 1];
            System.arraycopy(p.pt, 0, npt, 0, p.pt.length);
            for (int i = p.pt.length; i < npt.length; i++) npt[i] = new PageTableEntry();
            p.pt = npt;
        }
    }

    
    public static class StatsAcc { public int swaps = 0; }
}
