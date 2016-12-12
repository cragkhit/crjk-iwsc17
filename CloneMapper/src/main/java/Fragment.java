import org.apache.commons.lang.builder.HashCodeBuilder;
public class Fragment {
    private String firstFile;
    private String secondFile;
    private int fStart;
    private int fEnd;
    private int sStart;
    private int sEnd;
    private String other;
    public Fragment() {
    }
    public Fragment ( String firstFile, int fStart, int fEnd, String secondFile, int sStart, int sEnd ) {
        this.firstFile = firstFile;
        this.secondFile = secondFile;
        this.fStart = fStart;
        this.fEnd = fEnd;
        this.sStart = sStart;
        this.sEnd = sEnd;
    }
    public boolean isMatch ( Fragment f ) {
        if ( this.firstFile.equals ( f.getFirstFile() ) && this.secondFile.equals ( f.getSecondFile() ) )
            if ( ( isInRange ( this.fStart, this.fEnd, f.fStart, f.fEnd ) && isInRange ( this.sStart, this.sEnd, f.sStart, f.sEnd ) )
                    ||
                    ( isInRange ( f.fStart, f.fEnd, this.fStart, this.fEnd ) && isInRange ( f.sStart, f.sEnd, this.sStart, this.sEnd ) ) ) {
                return true;
            }
        return false;
    }
    public boolean isInRange ( int s1, int e1, int s2, int e2 ) {
        if ( s2 >= s1 && e2 <= e1 ) {
            return true;
        } else {
            return false;
        }
    }
    public double[] getOverlap ( Fragment cf2 ) {
        int minFStart, maxFStart, minFEnd, maxFEnd = -1;
        int minSStart, maxSStart, minSEnd, maxSEnd = -1;
        if ( this.getfStart() <= cf2.getfStart() ) {
            minFStart = this.getfStart();
            maxFStart = cf2.getfStart();
        } else {
            minFStart = cf2.getfStart();
            maxFStart = this.getfStart();
        }
        if ( this.getfEnd() <= cf2.getfEnd() ) {
            minFEnd = this.getfEnd();
            maxFEnd = cf2.getfEnd();
        } else {
            minFEnd = cf2.getfEnd();
            maxFEnd = this.getfEnd();
        }
        if ( this.getsStart() <= cf2.getsStart() ) {
            minSStart = this.getsStart();
            maxSStart = cf2.getsStart();
        } else {
            minSStart = cf2.getsStart();
            maxSStart = this.getsStart();
        }
        if ( this.getsEnd() <= cf2.getsEnd() ) {
            minSEnd = this.getsEnd();
            maxSEnd = cf2.getsEnd();
        } else {
            minSEnd = cf2.getsEnd();
            maxSEnd = this.getsEnd();
        }
        double[] overlap = new double[2];
        overlap[0] = ( double ) ( minFEnd - maxFStart + 1 ) / ( maxFEnd - minFStart + 1 );
        overlap[1] = ( double ) ( minSEnd - maxSStart + 1 ) / ( maxSEnd - minSStart + 1 );
        return overlap;
    }
    public double[] getContained ( Fragment cf2 ) {
        int minFStart, maxFStart, minFEnd, maxFEnd = -1;
        int minSStart, maxSStart, minSEnd, maxSEnd = -1;
        if ( this.getfStart() <= cf2.getfStart() ) {
            minFStart = this.getfStart();
            maxFStart = cf2.getfStart();
        } else {
            minFStart = cf2.getfStart();
            maxFStart = this.getfStart();
        }
        if ( this.getfEnd() <= cf2.getfEnd() ) {
            minFEnd = this.getfEnd();
            maxFEnd = cf2.getfEnd();
        } else {
            minFEnd = cf2.getfEnd();
            maxFEnd = this.getfEnd();
        }
        if ( this.getsStart() <= cf2.getsStart() ) {
            minSStart = this.getsStart();
            maxSStart = cf2.getsStart();
        } else {
            minSStart = cf2.getsStart();
            maxSStart = this.getsStart();
        }
        if ( this.getsEnd() <= cf2.getsEnd() ) {
            minSEnd = this.getsEnd();
            maxSEnd = cf2.getsEnd();
        } else {
            minSEnd = cf2.getsEnd();
            maxSEnd = this.getsEnd();
        }
        double[] contained = new double[2];
        contained[0] = ( double ) ( minFEnd - maxFStart + 1 ) / ( this.getfEnd() - this.getfStart() + 1 );
        contained[1] = ( double ) ( minSEnd - maxSStart + 1 ) / ( this.getsEnd() - this.getsStart() + 1 );
        return contained;
    }
    public String getFirstFile() {
        return firstFile;
    }
    public String getSecondFile() {
        return secondFile;
    }
    public int getfStart() {
        return fStart;
    }
    public int getfEnd() {
        return fEnd;
    }
    public int getsStart() {
        return sStart;
    }
    public int getsEnd() {
        return sEnd;
    }
    public void setFirstFile ( String firstFile ) {
        this.firstFile = firstFile;
    }
    public void setSecondFile ( String secondFile ) {
        this.secondFile = secondFile;
    }
    public void setfStart ( int fStart ) {
        this.fStart = fStart;
    }
    public void setfEnd ( int fEnd ) {
        this.fEnd = fEnd;
    }
    public void setsStart ( int sStart ) {
        this.sStart = sStart;
    }
    public void setsEnd ( int sEnd ) {
        this.sEnd = sEnd;
    }
    public String getOther() {
        return other;
    }
    public void setOther ( String other ) {
        this.other = other;
    }
    public String toString() {
        return this.firstFile + ", " + this.fStart + ", " + this.fEnd
               + "," + this.secondFile + "," + this.sStart + "," + this.sEnd;
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder ( 17, 37 ).
               append ( firstFile ).
               append ( secondFile ).
               append ( fStart ).
               append ( fEnd ).
               append ( sStart ).
               append ( sEnd ).
               toHashCode();
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( !Fragment.class.isAssignableFrom ( obj.getClass() ) ) {
            return false;
        }
        final Fragment other = ( Fragment ) obj;
        if ( ( this.firstFile == null ) ? ( other.firstFile != null ) : !this.firstFile.equals ( other.firstFile ) ) {
            return false;
        }
        if ( this.fStart != other.fStart ) {
            return false;
        }
        if ( this.fEnd != other.fEnd ) {
            return false;
        }
        if ( this.sStart != other.sStart ) {
            return false;
        }
        if ( this.sEnd != other.sEnd ) {
            return false;
        }
        return true;
    }
    public boolean equalsOffByOne ( Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( !Fragment.class.isAssignableFrom ( obj.getClass() ) ) {
            return false;
        }
        final Fragment other = ( Fragment ) obj;
        if ( ( this.firstFile == null ) ? ( other.firstFile != null ) : !this.firstFile.equals ( other.firstFile ) ) {
            return false;
        }
        if ( Math.abs ( this.fStart - other.fStart ) > 1 ) {
            return false;
        }
        if ( this.fEnd != other.fEnd ) {
            return false;
        }
        if ( Math.abs ( this.sStart - other.sStart ) > 1 ) {
            return false;
        }
        if ( this.sEnd != other.sEnd ) {
            return false;
        }
        return true;
    }
    public int getMinCloneLine() {
        return Math.min ( ( this.fEnd - this.fStart + 1 ), ( this.sEnd - this.sStart + 1 ) );
    }
}
