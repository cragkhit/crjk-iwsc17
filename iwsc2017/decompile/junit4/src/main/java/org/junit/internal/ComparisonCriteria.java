package org.junit.internal;
import java.lang.reflect.Array;
import org.junit.Assert;
import java.util.Arrays;
public abstract class ComparisonCriteria {
    private static final Object END_OF_ARRAY_SENTINEL;
    public void arrayEquals ( final String message, final Object expecteds, final Object actuals ) throws ArrayComparisonFailure {
        this.arrayEquals ( message, expecteds, actuals, true );
    }
    private void arrayEquals ( final String message, final Object expecteds, final Object actuals, final boolean outer ) throws ArrayComparisonFailure {
        if ( expecteds == actuals || Arrays.deepEquals ( new Object[] { expecteds }, new Object[] { actuals } ) ) {
            return;
        }
        String header = ( message == null ) ? "" : ( message + ": " );
        final String exceptionMessage = outer ? header : "";
        if ( expecteds == null ) {
            Assert.fail ( exceptionMessage + "expected array was null" );
        }
        if ( actuals == null ) {
            Assert.fail ( exceptionMessage + "actual array was null" );
        }
        final int actualsLength = Array.getLength ( actuals );
        final int expectedsLength = Array.getLength ( expecteds );
        if ( actualsLength != expectedsLength ) {
            header = header + "array lengths differed, expected.length=" + expectedsLength + " actual.length=" + actualsLength + "; ";
        }
        final int prefixLength = Math.min ( actualsLength, expectedsLength );
        for ( int i = 0; i < prefixLength; ++i ) {
            final Object expected = Array.get ( expecteds, i );
            final Object actual = Array.get ( actuals, i );
            if ( this.isArray ( expected ) && this.isArray ( actual ) ) {
                try {
                    this.arrayEquals ( message, expected, actual, false );
                    continue;
                } catch ( ArrayComparisonFailure e ) {
                    e.addDimension ( i );
                    throw e;
                } catch ( AssertionError e2 ) {
                    throw new ArrayComparisonFailure ( header, e2, i );
                }
            }
            try {
                this.assertElementsEqual ( expected, actual );
            } catch ( AssertionError e2 ) {
                throw new ArrayComparisonFailure ( header, e2, i );
            }
        }
        if ( actualsLength != expectedsLength ) {
            final Object expected2 = this.getToStringableArrayElement ( expecteds, expectedsLength, prefixLength );
            final Object actual2 = this.getToStringableArrayElement ( actuals, actualsLength, prefixLength );
            try {
                Assert.assertEquals ( expected2, actual2 );
            } catch ( AssertionError e3 ) {
                throw new ArrayComparisonFailure ( header, e3, prefixLength );
            }
        }
    }
    private Object getToStringableArrayElement ( final Object array, final int length, final int index ) {
        if ( index >= length ) {
            return ComparisonCriteria.END_OF_ARRAY_SENTINEL;
        }
        final Object element = Array.get ( array, index );
        if ( this.isArray ( element ) ) {
            return objectWithToString ( this.componentTypeName ( element.getClass() ) + "[" + Array.getLength ( element ) + "]" );
        }
        return element;
    }
    private static Object objectWithToString ( final String string ) {
        return new Object() {
            public String toString() {
                return string;
            }
        };
    }
    private String componentTypeName ( final Class<?> arrayClass ) {
        final Class<?> componentType = arrayClass.getComponentType();
        if ( componentType.isArray() ) {
            return this.componentTypeName ( componentType ) + "[]";
        }
        return componentType.getName();
    }
    private boolean isArray ( final Object expected ) {
        return expected != null && expected.getClass().isArray();
    }
    protected abstract void assertElementsEqual ( final Object p0, final Object p1 );
    static {
        END_OF_ARRAY_SENTINEL = objectWithToString ( "end of array" );
    }
}
