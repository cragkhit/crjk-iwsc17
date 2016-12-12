// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.simpletag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import java.io.Writer;
import javax.servlet.jsp.tagext.JspFragment;
import java.util.Random;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ShuffleSimpleTag extends SimpleTagSupport
{
    private static final Random random;
    private JspFragment fragment1;
    private JspFragment fragment2;
    private JspFragment fragment3;
    
    public void doTag() throws JspException, IOException {
        switch (ShuffleSimpleTag.random.nextInt(6)) {
            case 0: {
                this.fragment1.invoke((Writer)null);
                this.fragment2.invoke((Writer)null);
                this.fragment3.invoke((Writer)null);
                break;
            }
            case 1: {
                this.fragment1.invoke((Writer)null);
                this.fragment3.invoke((Writer)null);
                this.fragment2.invoke((Writer)null);
                break;
            }
            case 2: {
                this.fragment2.invoke((Writer)null);
                this.fragment1.invoke((Writer)null);
                this.fragment3.invoke((Writer)null);
                break;
            }
            case 3: {
                this.fragment2.invoke((Writer)null);
                this.fragment3.invoke((Writer)null);
                this.fragment1.invoke((Writer)null);
                break;
            }
            case 4: {
                this.fragment3.invoke((Writer)null);
                this.fragment1.invoke((Writer)null);
                this.fragment2.invoke((Writer)null);
                break;
            }
            case 5: {
                this.fragment3.invoke((Writer)null);
                this.fragment2.invoke((Writer)null);
                this.fragment1.invoke((Writer)null);
                break;
            }
        }
    }
    
    public void setFragment1(final JspFragment fragment1) {
        this.fragment1 = fragment1;
    }
    
    public void setFragment2(final JspFragment fragment2) {
        this.fragment2 = fragment2;
    }
    
    public void setFragment3(final JspFragment fragment3) {
        this.fragment3 = fragment3;
    }
    
    static {
        random = new Random();
    }
}
