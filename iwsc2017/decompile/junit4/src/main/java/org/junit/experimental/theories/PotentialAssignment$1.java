package org.junit.experimental.theories;
static final class PotentialAssignment$1 extends PotentialAssignment {
    final   Object val$value;
    final   String val$name;
    public Object getValue() {
        return this.val$value;
    }
    public String toString() {
        return String.format ( "[%s]", this.val$value );
    }
    public String getDescription() {
        String valueString;
        if ( this.val$value == null ) {
            valueString = "null";
        } else {
            try {
                valueString = String.format ( "\"%s\"", this.val$value );
            } catch ( Throwable e ) {
                valueString = String.format ( "[toString() threw %s: %s]", e.getClass().getSimpleName(), e.getMessage() );
            }
        }
        return String.format ( "%s <from %s>", valueString, this.val$name );
    }
}
