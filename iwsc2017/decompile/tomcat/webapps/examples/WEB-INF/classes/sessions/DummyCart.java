// 
// Decompiled by Procyon v0.5.29
// 

package sessions;

import java.util.Vector;

public class DummyCart
{
    final Vector<String> v;
    String submit;
    String item;
    
    public DummyCart() {
        this.v = new Vector<String>();
        this.submit = null;
        this.item = null;
    }
    
    private void addItem(final String name) {
        this.v.addElement(name);
    }
    
    private void removeItem(final String name) {
        this.v.removeElement(name);
    }
    
    public void setItem(final String name) {
        this.item = name;
    }
    
    public void setSubmit(final String s) {
        this.submit = s;
    }
    
    public String[] getItems() {
        final String[] s = new String[this.v.size()];
        this.v.copyInto(s);
        return s;
    }
    
    public void processRequest() {
        if (this.submit == null || this.submit.equals("add")) {
            this.addItem(this.item);
        }
        else if (this.submit.equals("remove")) {
            this.removeItem(this.item);
        }
        this.reset();
    }
    
    private void reset() {
        this.submit = null;
        this.item = null;
    }
}
