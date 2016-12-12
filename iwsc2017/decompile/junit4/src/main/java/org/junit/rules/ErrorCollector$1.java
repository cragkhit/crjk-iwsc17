package org.junit.rules;
import org.junit.Assert;
import org.hamcrest.Matcher;
import java.util.concurrent.Callable;
class ErrorCollector$1 implements Callable<Object> {
    final   String val$reason;
    final   Object val$value;
    final   Matcher val$matcher;
    public Object call() throws Exception {
        Assert.assertThat ( this.val$reason, this.val$value, ( org.hamcrest.Matcher<? super Object> ) this.val$matcher );
        return this.val$value;
    }
}
