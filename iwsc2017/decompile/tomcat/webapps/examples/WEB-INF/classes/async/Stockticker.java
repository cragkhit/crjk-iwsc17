// 
// Decompiled by Procyon v0.5.29
// 

package async;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Stockticker implements Runnable
{
    public volatile boolean run;
    protected final AtomicInteger counter;
    final ArrayList<TickListener> listeners;
    protected volatile Thread ticker;
    protected volatile int ticknr;
    
    public Stockticker() {
        this.run = true;
        this.counter = new AtomicInteger(0);
        this.listeners = new ArrayList<TickListener>();
        this.ticker = null;
        this.ticknr = 0;
    }
    
    public synchronized void start() {
        this.run = true;
        (this.ticker = new Thread(this)).setName("Ticker Thread");
        this.ticker.start();
    }
    
    public synchronized void stop() {
        this.run = false;
        try {
            this.ticker.join();
        }
        catch (InterruptedException x) {
            Thread.interrupted();
        }
        this.ticker = null;
    }
    
    public void addTickListener(final TickListener listener) {
        if (this.listeners.add(listener) && this.counter.incrementAndGet() == 1) {
            this.start();
        }
    }
    
    public void removeTickListener(final TickListener listener) {
        if (this.listeners.remove(listener) && this.counter.decrementAndGet() == 0) {
            this.stop();
        }
    }
    
    @Override
    public void run() {
        try {
            final Stock[] stocks = { new Stock("GOOG", 435.43), new Stock("YHOO", 27.88), new Stock("ASF", 1015.55) };
            final Random r = new Random(System.currentTimeMillis());
            while (this.run) {
                for (int j = 0; j < 1; ++j) {
                    int i = r.nextInt() % 3;
                    if (i < 0) {
                        i *= -1;
                    }
                    final Stock stock = stocks[i];
                    final double change = r.nextDouble();
                    final boolean plus = r.nextBoolean();
                    if (plus) {
                        stock.setValue(stock.getValue() + change);
                    }
                    else {
                        stock.setValue(stock.getValue() - change);
                    }
                    stock.setCnt(++this.ticknr);
                    for (final TickListener l : this.listeners) {
                        l.tick(stock);
                    }
                }
                Thread.sleep(850L);
            }
        }
        catch (InterruptedException ex) {}
        catch (Exception x) {
            x.printStackTrace();
        }
    }
    
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
    
    public interface TickListener
    {
        void tick(Stock p0);
    }
}
