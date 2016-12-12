package org.apache.catalina.ssi;
import java.text.DecimalFormat;
import java.io.IOException;
import java.io.PrintWriter;
public final class SSIFsize implements SSICommand {
    static final int ONE_KILOBYTE = 1024;
    static final int ONE_MEGABYTE = 1048576;
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) {
        long lastModified = 0L;
        final String configErrMsg = ssiMediator.getConfigErrMsg();
        for ( int i = 0; i < paramNames.length; ++i ) {
            final String paramName = paramNames[i];
            final String paramValue = paramValues[i];
            final String substitutedValue = ssiMediator.substituteVariables ( paramValue );
            try {
                if ( paramName.equalsIgnoreCase ( "file" ) || paramName.equalsIgnoreCase ( "virtual" ) ) {
                    final boolean virtual = paramName.equalsIgnoreCase ( "virtual" );
                    lastModified = ssiMediator.getFileLastModified ( substitutedValue, virtual );
                    final long size = ssiMediator.getFileSize ( substitutedValue, virtual );
                    final String configSizeFmt = ssiMediator.getConfigSizeFmt();
                    writer.write ( this.formatSize ( size, configSizeFmt ) );
                } else {
                    ssiMediator.log ( "#fsize--Invalid attribute: " + paramName );
                    writer.write ( configErrMsg );
                }
            } catch ( IOException e ) {
                ssiMediator.log ( "#fsize--Couldn't get size for file: " + substitutedValue, e );
                writer.write ( configErrMsg );
            }
        }
        return lastModified;
    }
    public String repeat ( final char aChar, final int numChars ) {
        if ( numChars < 0 ) {
            throw new IllegalArgumentException ( "Num chars can't be negative" );
        }
        final StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < numChars; ++i ) {
            buf.append ( aChar );
        }
        return buf.toString();
    }
    public String padLeft ( final String str, final int maxChars ) {
        String result = str;
        final int charsToAdd = maxChars - str.length();
        if ( charsToAdd > 0 ) {
            result = this.repeat ( ' ', charsToAdd ) + str;
        }
        return result;
    }
    protected String formatSize ( final long size, final String format ) {
        String retString = "";
        if ( format.equalsIgnoreCase ( "bytes" ) ) {
            final DecimalFormat decimalFormat = new DecimalFormat ( "#,##0" );
            retString = decimalFormat.format ( size );
        } else {
            if ( size == 0L ) {
                retString = "0k";
            } else if ( size < 1024L ) {
                retString = "1k";
            } else if ( size < 1048576L ) {
                retString = Long.toString ( ( size + 512L ) / 1024L );
                retString += "k";
            } else if ( size < 103809024L ) {
                final DecimalFormat decimalFormat = new DecimalFormat ( "0.0M" );
                retString = decimalFormat.format ( size / 1048576.0 );
            } else {
                retString = Long.toString ( ( size + 541696L ) / 1048576L );
                retString += "M";
            }
            retString = this.padLeft ( retString, 5 );
        }
        return retString;
    }
}
