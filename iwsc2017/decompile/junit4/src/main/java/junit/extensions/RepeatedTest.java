package junit.extensions;
import junit.framework.TestResult;
import junit.framework.Test;
public class RepeatedTest extends TestDecorator {
    private int fTimesRepeat;
    public RepeatedTest ( final Test test, final int repeat ) {
        super ( test );
        if ( repeat < 0 ) {
            throw new IllegalArgumentException ( "Repetition count must be >= 0" );
        }
        this.fTimesRepeat = repeat;
    }
    public int countTestCases() {
        return super.countTestCases() * this.fTimesRepeat;
    }
    public void run ( final TestResult result ) {
        for ( int i = 0; i < this.fTimesRepeat && !result.shouldStop(); ++i ) {
            super.run ( result );
        }
    }
    public String toString() {
        return super.toString() + "(repeated)";
    }
}
