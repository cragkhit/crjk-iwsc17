package org.apache.jasper.runtime;
import javax.servlet.jsp.PageContext;
private static final class PageContextPool {
    private final PageContext[] pool;
    private int current;
    public PageContextPool() {
        this.current = -1;
        this.pool = new PageContext[JspFactoryImpl.access$200()];
    }
    public void put ( final PageContext o ) {
        if ( this.current < JspFactoryImpl.access$200() - 1 ) {
            ++this.current;
            this.pool[this.current] = o;
        }
    }
    public PageContext get() {
        PageContext item = null;
        if ( this.current >= 0 ) {
            item = this.pool[this.current];
            --this.current;
        }
        return item;
    }
}
