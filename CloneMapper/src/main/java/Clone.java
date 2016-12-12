public class Clone {
    private String file;
    private int startline;
    private int endline;
    private boolean matched;
    private int origstartline;
    private int origendline;
    private int pcid;
    public Clone() {
    }
    public Clone ( String file, int startline, int endline, boolean matched, int origstartline, int origendline, int pcid ) {
        this.file = file;
        this.startline = startline;
        this.endline = endline;
        this.matched = matched;
        this.origstartline = origstartline;
        this.origendline = origendline;
        this.pcid = pcid;
    }
    public String toString() {
        return file + "," + origstartline + "," + origendline;
    }
    public String toStringOrig() {
        return file + "," + startline + "," + endline;
    }
    public String getFile() {
        return file;
    }
    public void setFile ( String file ) {
        this.file = file;
    }
    public int getStartline() {
        return startline;
    }
    public void setStartline ( int startline ) {
        this.startline = startline;
    }
    public int getEndline() {
        return endline;
    }
    public void setEndline ( int endline ) {
        this.endline = endline;
    }
    public boolean isMatched() {
        return matched;
    }
    public void setMatched ( boolean matched ) {
        this.matched = matched;
    }
    public int getOrigstartline() {
        return origstartline;
    }
    public void setOrigstartline ( int origstartline ) {
        this.origstartline = origstartline;
    }
    public int getOrigendline() {
        return origendline;
    }
    public void setOrigendline ( int origendline ) {
        this.origendline = origendline;
    }
    public int getPcid() {
        return pcid;
    }
    public void setPcid ( int pcid ) {
        this.pcid = pcid;
    }
}
