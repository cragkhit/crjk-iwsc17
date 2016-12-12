// 
// Decompiled by Procyon v0.5.29
// 

package validators;

import java.io.InputStream;
import java.io.IOException;
import javax.servlet.jsp.tagext.ValidationMessage;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagLibraryValidator;

public class DebugValidator extends TagLibraryValidator
{
    public ValidationMessage[] validate(final String prefix, final String uri, final PageData page) {
        System.out.println("---------- Prefix=" + prefix + " URI=" + uri + "----------");
        final InputStream is = page.getInputStream();
        try {
            while (true) {
                final int ch = is.read();
                if (ch < 0) {
                    break;
                }
                System.out.print((char)ch);
            }
        }
        catch (IOException e) {}
        System.out.println();
        System.out.println("-----------------------------------------------");
        return null;
    }
}
