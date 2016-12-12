// 
// Decompiled by Procyon v0.5.29
// 

package filters;

import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.GenericFilter;

public final class ExampleFilter extends GenericFilter
{
    private static final long serialVersionUID = 1L;
    private String attribute;
    
    public ExampleFilter() {
        this.attribute = null;
    }
    
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (this.attribute != null) {
            request.setAttribute(this.attribute, (Object)this);
        }
        final long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        final long stopTime = System.currentTimeMillis();
        this.getServletContext().log(this.toString() + ": " + (stopTime - startTime) + " milliseconds");
    }
    
    public void init() throws ServletException {
        this.attribute = this.getInitParameter("attribute");
    }
    
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimingFilter(");
        sb.append(this.getFilterConfig());
        sb.append(")");
        return sb.toString();
    }
}
