// 
// Decompiled by Procyon v0.5.29
// 

package async;

import java.text.DecimalFormat;

public static final class Stock implements Cloneable
{
    protected static final DecimalFormat df;
    protected final String symbol;
    protected double value;
    protected double lastchange;
    protected int cnt;
    
    public Stock(final String symbol, final double initvalue) {
        this.value = 0.0;
        this.lastchange = 0.0;
        this.cnt = 0;
        this.symbol = symbol;
        this.value = initvalue;
    }
    
    public void setCnt(final int c) {
        this.cnt = c;
    }
    
    public int getCnt() {
        return this.cnt;
    }
    
    public String getSymbol() {
        return this.symbol;
    }
    
    public double getValue() {
        return this.value;
    }
    
    public void setValue(final double value) {
        final double old = this.value;
        this.value = value;
        this.lastchange = value - old;
    }
    
    public String getValueAsString() {
        return Stock.df.format(this.value);
    }
    
    public double getLastChange() {
        return this.lastchange;
    }
    
    public void setLastChange(final double lastchange) {
        this.lastchange = lastchange;
    }
    
    public String getLastChangeAsString() {
        return Stock.df.format(this.lastchange);
    }
    
    @Override
    public int hashCode() {
        return this.symbol.hashCode();
    }
    
    @Override
    public boolean equals(final Object other) {
        return other instanceof Stock && this.symbol.equals(((Stock)other).symbol);
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("STOCK#");
        buf.append(this.getSymbol());
        buf.append("#");
        buf.append(this.getValueAsString());
        buf.append("#");
        buf.append(this.getLastChangeAsString());
        buf.append("#");
        buf.append(String.valueOf(this.getCnt()));
        return buf.toString();
    }
    
    public Object clone() {
        final Stock s = new Stock(this.getSymbol(), this.getValue());
        s.setLastChange(this.getLastChange());
        s.setCnt(this.cnt);
        return s;
    }
    
    static {
        df = new DecimalFormat("0.00");
    }
}
