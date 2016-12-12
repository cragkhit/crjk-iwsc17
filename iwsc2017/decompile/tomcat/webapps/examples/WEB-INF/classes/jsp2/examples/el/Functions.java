// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples.el;

import java.util.Locale;

public class Functions
{
    public static String reverse(final String text) {
        return new StringBuilder(text).reverse().toString();
    }
    
    public static int numVowels(final String text) {
        final String vowels = "aeiouAEIOU";
        int result = 0;
        for (int i = 0; i < text.length(); ++i) {
            if (vowels.indexOf(text.charAt(i)) != -1) {
                ++result;
            }
        }
        return result;
    }
    
    public static String caps(final String text) {
        return text.toUpperCase(Locale.ENGLISH);
    }
}
