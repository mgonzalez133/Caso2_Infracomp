
public class Frame {
    public int id;               
    public boolean free = true;  
    public int ownerProc = -1;   
    public int vpn = -1;        
    public long lastAccess = -1L; 

    public Frame(int id) { this.id = id; }

    @Override
    public String toString() {
        return "F" + id + "{p=" + ownerProc + ", vpn=" + vpn + ", free=" + free + "}";
    }
}
