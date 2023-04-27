
public class Signature implements Comparable<Signature> {
    public String mnemonic;
    public String operands;
    public String operandsDoc;
    public String cpuFlags;
    public String description;

    public Signature(String mnemonic, String operands, String cpuFlags, String operandsDoc, String description) {
        this.mnemonic = mnemonic;
        this.operands = operands;
        this.operandsDoc = operandsDoc;
        this.cpuFlags = cpuFlags;
        this.description = description;
    }

    public int compareTo(Signature o) {
        final int i1 = this.mnemonic.compareTo(o.mnemonic);
        if (i1 != 0) return i1;
        return this.operands.compareTo(o.operands);
    }
}

