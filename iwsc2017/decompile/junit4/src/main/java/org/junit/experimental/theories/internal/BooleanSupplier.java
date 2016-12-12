package org.junit.experimental.theories.internal;
import java.util.Arrays;
import org.junit.experimental.theories.PotentialAssignment;
import java.util.List;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
public class BooleanSupplier extends ParameterSupplier {
    public List<PotentialAssignment> getValueSources ( final ParameterSignature sig ) {
        return Arrays.asList ( PotentialAssignment.forValue ( "true", true ), PotentialAssignment.forValue ( "false", false ) );
    }
}
