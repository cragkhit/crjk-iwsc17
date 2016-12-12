// 
// Decompiled by Procyon v0.5.29
// 

package num;

import java.util.Random;
import java.io.Serializable;

public class NumberGuessBean implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int answer;
    private String hint;
    private int numGuesses;
    private boolean success;
    private final Random random;
    
    public NumberGuessBean() {
        this.random = new Random();
        this.reset();
    }
    
    public int getAnswer() {
        return this.answer;
    }
    
    public void setAnswer(final int answer) {
        this.answer = answer;
    }
    
    public String getHint() {
        return "" + this.hint;
    }
    
    public void setHint(final String hint) {
        this.hint = hint;
    }
    
    public void setNumGuesses(final int numGuesses) {
        this.numGuesses = numGuesses;
    }
    
    public int getNumGuesses() {
        return this.numGuesses;
    }
    
    public boolean getSuccess() {
        return this.success;
    }
    
    public void setSuccess(final boolean success) {
        this.success = success;
    }
    
    public void setGuess(final String guess) {
        ++this.numGuesses;
        int g;
        try {
            g = Integer.parseInt(guess);
        }
        catch (NumberFormatException e) {
            g = -1;
        }
        if (g == this.answer) {
            this.success = true;
        }
        else if (g == -1) {
            this.hint = "a number next time";
        }
        else if (g < this.answer) {
            this.hint = "higher";
        }
        else if (g > this.answer) {
            this.hint = "lower";
        }
    }
    
    public void reset() {
        this.answer = Math.abs(this.random.nextInt() % 100) + 1;
        this.success = false;
        this.numGuesses = 0;
    }
}
