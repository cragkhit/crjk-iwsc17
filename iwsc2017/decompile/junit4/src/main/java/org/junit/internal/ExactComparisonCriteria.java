package org.junit.internal;
import org.junit.Assert;
public class ExactComparisonCriteria extends ComparisonCriteria {
    protected void assertElementsEqual ( final Object expected, final Object actual ) {
        Assert.assertEquals ( expected, actual );
    }
}
