

public class Ref {
    public final int matrix;   
    public final int i, j;     
    public final int vpn;      
    public final int offset;   
    public final char op;      

    public Ref(int matrix, int i, int j, int vpn, int offset, char op) {
        this.matrix = matrix;
        this.i = i;
        this.j = j;
        this.vpn = vpn;
        this.offset = offset;
        this.op = op;
    }

    @Override
    public String toString() {
        return "M" + matrix + ":[" + i + "-" + j + "]," + vpn + "," + offset + "," + op;
    }
}

