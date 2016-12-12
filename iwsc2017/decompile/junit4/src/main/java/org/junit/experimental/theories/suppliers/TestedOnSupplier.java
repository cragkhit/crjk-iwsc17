package org.junit.experimental.theories.suppliers;
import java.util.ArrayList;
import org.junit.experimental.theories.PotentialAssignment;
import java.util.List;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
public class TestedOnSupplier extends ParameterSupplier {
    public List<PotentialAssignment> getValueSources ( final ParameterSignature sig ) {
        final List<PotentialAssignment> list = new ArrayList<PotentialAssignment>();
        final TestedOn testedOn = sig.getAnnotation ( TestedOn.class );
        final int[] ints2;
        final int[] ints = ints2 = testedOn.ints();
        for ( final int i : ints2 ) {
            list.add ( PotentialAssignment.forValue ( "ints", i ) );
        }
        return list;
    }
}
